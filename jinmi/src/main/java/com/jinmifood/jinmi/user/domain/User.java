package com.jinmifood.jinmi.user.domain;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long id; // 유저번호 pk

    @Column(nullable = false, unique = true) // 이메일은 고유
    private String email; // 이메일 로그인 시 Id 용도

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String address; // 주소

    @Column
    private long pointId; // 포인트 id 포인트 테이블 참조

    @Column(nullable = false)
    private String displayName; // 닉네임

    @Column
    private Long totalOrderCnt; // 누적 주문 수

    @Column(nullable = true, unique = true)
    private String phoneNumber; // 전화번호

    @Column
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 회원구분

    @Column(nullable = true)
    private String provider;


    @Column(nullable = true, unique = true)
    private Long kakaoId; // 카카오 고유 ID (카카오 로그인 전용)

    @Column(nullable = true, unique = true)
    private String naverId;

    @Column(length = 512, nullable = true)
    private String kakaoRefreshToken; // 카카오 리프레시 토큰

    @Column(length = 512, nullable = true)
    private String googleRefreshToken;

    @Column(length = 512, nullable = true)
    private String naverRefreshToken;

    public void updateGoogleRefreshToken(String googleRefreshToken) {
        this.googleRefreshToken = googleRefreshToken;
    }

    // Google Refresh Token 삭제 메서드 (탈퇴 시 사용)
    public void clearGoogleRefreshToken() {
        this.googleRefreshToken = null;
    }

    public void updateKakaoRefreshToken(String kakaoRefreshToken) {
        this.kakaoRefreshToken = kakaoRefreshToken;
    }

    public void updateNaverRefreshToken(String naverRefreshToken) { this.naverRefreshToken = naverRefreshToken; }

    public void clearKakaoRefreshToken() {
        this.kakaoRefreshToken = null;
    }
    public void clearKakaoId() {
        this.kakaoId = null;
    }

    public void clearNaverRefreshToken() { this.naverRefreshToken = null; }
    public void clearNaverId() { this.naverId = null; }
    @Column
    private LocalDateTime createAt; // 가입날짜

    public void updatePassword(String encodedNewPassword) { this.password = encodedNewPassword; }

    public void updateDetails(String displayName, String phoneNumber, String address) {
        if( displayName != null ) {
            this.displayName = displayName;
        }
        if( phoneNumber != null ) {
            this.phoneNumber = phoneNumber;
        }
        if( address != null ) {
            this.address = address;
        }
    }
//    public User update(String displayName) {
//        this.displayName = displayName;
//        return this;
//    }
    public enum Role {
        USER, ADMIN
    }

    public void updateLastLoginAt(){
        this.lastLoginAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        this.createAt = this.createAt == null? LocalDateTime.now() : this.createAt;
        this.lastLoginAt = this.lastLoginAt == null? LocalDateTime.now() : this.lastLoginAt;
        this.role = this.role == null? Role.USER : this.role;
        this.totalOrderCnt = this.totalOrderCnt == null? 0 : this.totalOrderCnt;
        this.provider = this.provider == null ? "local" : this.provider;

    }


}

