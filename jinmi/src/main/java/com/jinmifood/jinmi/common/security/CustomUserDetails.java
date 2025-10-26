package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.user.domain.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Getter // Lombok Getter 추가
public class CustomUserDetails implements UserDetails, OAuth2User {

    // User 엔티티 필드를 직접 사용하도록 변경합니다.
    private final User user;

    //  OAuth2User 인터페이스 구현을 위한 필드
    private final Map<String, Object> attributes;

    // 1. 일반 로그인/기존 로직용 생성자 (attributes가 null인 경우)
    public CustomUserDetails(User user) {
        this(user, null);
    }

    // 2. OAuth2User 생성자 (CustomOAuth2UserService에서 호출)
    private CustomUserDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
        log.info("현재 ID = {}", user.getId());
    }

    // 3. OAuth2User용 정적 팩토리 메서드 (CustomOAuth2UserService에서 호출)
    public static CustomUserDetails create(User user, Map<String, Object> attributes) {
        return new CustomUserDetails(user, attributes);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // user.getRole().name()을 사용하여 권한 부여
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // UserDetails의 Username은 JWT에서 사용하는 식별자(이메일).
    }

    // 기존 getId(), getEmail() 메서드는 user 필드의 getter로 대체.
    public Long getId() {
        return user.getId();
    }
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }



    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String getName() {
        // 이 메서드는 UserDetails의 getUsername()과 다르며, 주로 OAuth2 시스템 내부에서 사용
        if (attributes != null && attributes.containsKey("sub")) {
            return (String) attributes.get("sub");
        }
        return user.getEmail(); // attributes가 없는 경우 이메일 반환
    }
}