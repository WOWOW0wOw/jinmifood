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

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            // Google의 기본 사용자 이름 속성 키는 'sub'입니다.
            return ofGoogle("sub", attributes);
        }
        // 향후 Kakao, Naver 추가 시 여기에 로직을 확장합니다.
        return null;
    }

    // User 엔티티로 변환하여 최초 회원가입에 사용
    public User toEntity(String role) {

        User.Role userRole = User.Role.valueOf(role.toUpperCase());
        // 소셜 로그인은 비밀번호 필드가 필요 없으므로 빈 문자열로 설정
        return User.builder()
                .email(email)
                .displayName(name)
                .password("")
                .provider(registrationId)
                .role(userRole)
                .address("미입력")
                .phoneNumber("000-0000-0000")
                .pointId(0L)
                .totalOrderCnt(0L)
                .build();
    }
}
