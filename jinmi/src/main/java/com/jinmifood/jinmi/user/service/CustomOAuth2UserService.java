package com.jinmifood.jinmi.user.service;

import com.jinmifood.jinmi.common.security.CustomUserDetails;
import com.jinmifood.jinmi.user.domain.User;
import com.jinmifood.jinmi.user.dto.OAuthAttributes;
import com.jinmifood.jinmi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error; // 필요
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    public static final String DUPLICATE_EMAIL_DIFFERENT_PROVIDER = "DUPLICATE_EMAIL_DIFFERENT_PROVIDER";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oauth2User.getAttributes());

        User user = saveOrUpdateWithProviderCheck(attributes);

        if (user == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("USER_PROCESS_FAILED"), "User object is null after processing.");
        }

        user.updateLastLoginAt();
        userRepository.save(user);

        return CustomUserDetails.create(user, oauth2User.getAttributes());
    }

    private String generateUniqueDisplayName(String baseName) {
        String uniqueName = baseName;
        int count = 0;

        while (userRepository.existsByDisplayName(uniqueName)) {
            count++;
            uniqueName = baseName + count;

            if (count > 999) {
                throw new IllegalStateException("닉네임 생성 실패: 1000회 이상 중복");
            }
        }
        return uniqueName;
    }


    private User saveOrUpdateWithProviderCheck(OAuthAttributes attributes) {

        String email = attributes.getEmail();
        String currentProvider = attributes.getRegistrationId().toUpperCase();

        Optional<User> existingUserOptional = userRepository.findByEmail(email);

        if (existingUserOptional.isPresent()) {
            User existingUser = existingUserOptional.get();
            String existingProvider = existingUser.getProvider() != null ? existingUser.getProvider().toUpperCase() : "LOCAL";

            if (!existingProvider.equalsIgnoreCase(currentProvider)) {

                String description = "이 이메일은 이미 " + existingProvider + " 계정으로 가입되어 있습니다. 기존 계정으로 로그인해주세요.";

                log.warn(" 이메일 중복 감지: 이메일={}, 기존 Provider={}, 시도 Provider={}",
                        email, existingProvider, currentProvider);

                throw new OAuth2AuthenticationException(
                        new OAuth2Error(DUPLICATE_EMAIL_DIFFERENT_PROVIDER),
                        description
                );
            }

            // 2. 이메일과 Provider가 일치하는 경우 (정상 로그인)
            return existingUser;

        } else {
            Optional<User> userOptionalBySocialId = Optional.empty();

            if ("kakao".equals(currentProvider) && attributes.getId() != null) {
                userOptionalBySocialId = userRepository.findByKakaoId(attributes.getId());
            } else if ("naver".equals(currentProvider) && attributes.getOAuth2Id() != null){
                userOptionalBySocialId = userRepository.findByNaverId(attributes.getOAuth2Id());
            }

            return userOptionalBySocialId
                    .map(entity -> {
                        return entity;
                    })
                    .orElseGet(() -> {
                        String baseName = attributes.getName();
                        String uniqueDisplayName = generateUniqueDisplayName(baseName);
                        return attributes.toEntity("USER", uniqueDisplayName);
                    });
        }
    }


    @Deprecated
    private User saveOrUpdate(OAuthAttributes attributes) {
        return null;
    }
}