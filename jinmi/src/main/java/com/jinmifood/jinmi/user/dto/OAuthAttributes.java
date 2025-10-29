package com.jinmifood.jinmi.user.dto;

import com.jinmifood.jinmi.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    private final Map<String, Object> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final String registrationId; // 'google'
    private final Long id;

    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        // Google은 name, email, picture 속성을 최상위에서 제공합니다.
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registrationId("google")
                .build();
    }
    private static OAuthAttributes ofKakao(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String name = "소셜 사용자";
        if (kakaoAccount != null && kakaoAccount.containsKey("profile")) {
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) kakaoProfile.get("nickname");
        }

        // 💡 [핵심]: 이메일 널 체크 로직
        String email = null;
        if (kakaoAccount != null) {
            Boolean hasEmail = (Boolean) kakaoAccount.get("has_email");
            if (hasEmail != null && hasEmail && kakaoAccount.containsKey("email")) {
                email = (String) kakaoAccount.get("email");
            }
        }

        return OAuthAttributes.builder()
                .name(name)
                .email(email)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName) // "id"
                .registrationId(registrationId) // "kakao"
                .id((Long) attributes.get(userNameAttributeName)) // 카카오 고유 ID (Long)
                .build();
    }

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle("sub", attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao(registrationId, "id", attributes);
        }
        // 향후 Kakao, Naver 추가 시 여기에 로직을 확장합니다.
        return null;
    }

    public User toEntity(String role) {

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        String finalEmail = email != null ? email : this.registrationId + "_user_" + this.id + "@social.com";

        User.UserBuilder userBuilder = User.builder()
                .email(finalEmail)
                .displayName(name)
                .password("") // 소셜 사용자는 비밀번호 없음
                .provider(registrationId)
                .role(userRole)
                .address("미입력")
                .phoneNumber(null)
                .pointId(0L)
                .totalOrderCnt(0L);

        if ("kakao".equals(registrationId)) {
            userBuilder.kakaoId(this.id);
        }

        return userBuilder.build();
    }

    // CustomOAuth2UserService에서 사용
    public User toEntity(String role, String uniqueDisplayName) {

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        String finalEmail = email != null ? email : this.registrationId + "_user_" + this.id + "@social.com";

        User.UserBuilder userBuilder = User.builder()
                .email(finalEmail)
                .displayName(uniqueDisplayName) //  인자로 받은 중복 없는 닉네임 사용
                .password("")
                .provider(registrationId)
                .role(userRole)
                .address("미입력")
                .phoneNumber(null)
                .pointId(0L)
                .totalOrderCnt(0L);

        if ("kakao".equals(registrationId)) {
            userBuilder.kakaoId(this.id);
        }

        return userBuilder.build();
    }
}
