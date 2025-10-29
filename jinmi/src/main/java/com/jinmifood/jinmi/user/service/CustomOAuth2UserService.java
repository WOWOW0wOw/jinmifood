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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oauth2User.getAttributes());

        User user = saveOrUpdateWithProviderCheck(attributes);

        user.updateLastLoginAt();
        userRepository.save(user);

        return CustomUserDetails.create(user, oauth2User.getAttributes());
    }
    private String generateUniqueDisplayName(String baseName) {
        String uniqueName = baseName;
        int count = 0;

        // 중복되는 닉네임이 없을 때까지 반복합니다.
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

        Optional<User> userOptional;

        if ("kakao".equals(attributes.getRegistrationId())) {
            userOptional = userRepository.findByKakaoId(attributes.getId());
        } else {
            userOptional = userRepository.findByEmail(attributes.getEmail());
        }

        return userOptional
                .map(entity -> {
                    return entity;
                })
                .orElseGet(() -> {
                    String baseName = attributes.getName();
                    String uniqueDisplayName = generateUniqueDisplayName(baseName);

                    return attributes.toEntity("USER", uniqueDisplayName);
                });
    }

    @Deprecated
    private User saveOrUpdate(OAuthAttributes attributes) {
        return null;
    }
}