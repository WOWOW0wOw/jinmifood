package com.jinmifood.jinmi.common.security.refreshToken.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refreshToken")
public class RefreshToken {

    @Id
    @Column(name = "userIdentifier", nullable = false)
    private String userIdentifier; // PK : 사용자 ID(이메일)

    @Column(name = "tokenValue", nullable = false, length = 500)
    private String tokenValue; // Refresh Token 문자열

    @Column(name = "expiryDate",nullable = false)
    private LocalDateTime expiryDate; // 토큰 만료시점

    @Builder
    public RefreshToken(String userIdentifier, String tokenValue, LocalDateTime expiryDate) {
        this.userIdentifier = userIdentifier;
        this.tokenValue = tokenValue;
        this.expiryDate = expiryDate;
    }

    public void updateToken(String newTokenValue, LocalDateTime newExpiryDate) {
        this.tokenValue = newTokenValue;
        this.expiryDate = newExpiryDate;
    }
}
