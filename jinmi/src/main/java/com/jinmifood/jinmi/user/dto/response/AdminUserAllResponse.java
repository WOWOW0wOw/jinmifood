package com.jinmifood.jinmi.user.dto.response;

import com.jinmifood.jinmi.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserAllResponse {
    private Long userId;
    private String email;
    private String displayName;
    private String phoneNumber;
    private String address;
    private String provider;
    private String role;
    private LocalDateTime createAt;
    private LocalDateTime lastLoginAt;

    public static AdminUserAllResponse from(User user){
        return new AdminUserAllResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getProvider(),
                user.getRole() != null ? user.getRole().name() : "USER",
                user.getCreateAt(),
                user.getLastLoginAt()
        );
    }
}
