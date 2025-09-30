package com.jinmifood.jinmi.common.security.refreshToken.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blacklistToken")
public class BlacklistToken {

    @Id
    @Column(name = "accessToken",nullable = false,length = 500)
    private String accessToken; //Pk AccessToken 값 자체

    @Column(name = "userIdentifier")
    private String userIdentifier; // AccessToken 남은 유효기간

    @Column(name = "expiryTime")
    private Long expiryTime; // AccessToken 남은시간

    @Builder
    public BlacklistToken(String accessToken, String userIdentifier, Long expiryTime) {
        this.accessToken = accessToken;
        this.userIdentifier = userIdentifier;
        this.expiryTime = expiryTime;
    }
}
