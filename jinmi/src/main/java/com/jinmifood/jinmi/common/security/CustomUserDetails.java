package com.jinmifood.jinmi.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Slf4j
public class CustomUserDetails extends User {
    private final Long id;
    private final String email;

    public CustomUserDetails(Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.id = id;
        this.email = email;
        log.info("현재 ID = {}", id);
    }
   public Long getId() {
        return id;
   }
    public String getEmail() {
        return email;
    }

}
