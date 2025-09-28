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

    @Column(nullable = false)
    private String phoneNumber; // 전화번호

    @Column
    private LocalDateTime lastLoginAt; // 마지막 로그인 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 회원구분

    @Column
    private LocalDateTime createAt; // 가입날짜

    public enum Role {
        USER, ADMIN
    }

    @PrePersist
    public void prePersist() {
        this.createAt = this.createAt == null? LocalDateTime.now() : this.createAt;
        this.lastLoginAt = this.lastLoginAt == null? LocalDateTime.now() : this.lastLoginAt;
        this.role = this.role == null? Role.USER : this.role;
        this.totalOrderCnt = this.totalOrderCnt == null? 0 : this.totalOrderCnt;

    }


}

