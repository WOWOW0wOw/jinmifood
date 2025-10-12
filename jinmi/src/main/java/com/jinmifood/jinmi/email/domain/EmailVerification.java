package com.jinmifood.jinmi.email.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private LocalDateTime createdAt; // 생성 시간 (만료 시간 계산을 위해 사용)

    @Builder
    public EmailVerification(String email, String code) {
        this.email = email;
        this.code = code;
        this.createdAt = LocalDateTime.now();
    }

    // 인증 코드만 업데이트하는 메서드
    public void updateCode(String newCode) {
        this.code = newCode;
        this.createdAt = LocalDateTime.now(); // 코드 재발송 시 시간 갱신
    }
}
