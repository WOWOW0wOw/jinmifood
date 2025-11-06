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
    private final String registrationId; // 'google', 'kakao', 'naver'
    private final Long id; // 카카오 id용 (Long)
    private final String oAuth2Id; // 네이버 id용 (String)


    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registrationId("google")
                .build();
    }

    private static OAuthAttributes ofNaver(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        String naverId = (String) response.get("id");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .registrationId(registrationId)
                .oAuth2Id(naverId) // 네이버 고유 ID
                .build();
    }

    private static OAuthAttributes ofKakao(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String name = "소셜 사용자";
        if (kakaoAccount != null && kakaoAccount.containsKey("profile")) {
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) kakaoProfile.get("nickname");
        }

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
                .nameAttributeKey(userNameAttributeName)
                .registrationId(registrationId)
                .id((Long) attributes.get(userNameAttributeName))
                .build();
    }

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return ofGoogle("sub", attributes);
        }
        if ("kakao".equals(registrationId)) {
            return ofKakao(registrationId, "id", attributes);
        }
        if ("naver".equals(registrationId)) {
            return ofNaver(registrationId, "response", attributes);
        }
        return null;
    }

    public User toEntity(String role) {

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        String finalEmail = email != null ? email : this.registrationId + "_user_" + this.id + "@social.com";

        User.UserBuilder userBuilder = User.builder()
                .email(finalEmail)
                .displayName(name)
                .password("")
                .provider(registrationId.toUpperCase())
                .role(userRole)
                .address("미입력")
                .phoneNumber(null)
                .pointId(0L)
                .totalOrderCnt(0L);

        if ("kakao".equals(registrationId)) {
            userBuilder.kakaoId(this.id);
        }
        if ("naver".equals(registrationId)) {
            userBuilder.naverId(this.oAuth2Id);
        }

        return userBuilder.build();
    }


    public User toEntity(String role, String uniqueDisplayName) {

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        String finalEmail = email != null ? email : this.registrationId + "_user_" + this.id + "@social.com";

        User.UserBuilder userBuilder = User.builder()
                .email(finalEmail)
                .displayName(uniqueDisplayName)
                .password("")
                .provider(registrationId.toUpperCase())
                .role(userRole)
                .address("미입력")
                .phoneNumber(null)
                .pointId(0L)
                .totalOrderCnt(0L);

        if ("kakao".equals(registrationId)) {
            userBuilder.kakaoId(this.id);
        }

        if ("naver".equals(registrationId)) {
            userBuilder.naverId(this.oAuth2Id);
        }

        return userBuilder.build();
    }
}