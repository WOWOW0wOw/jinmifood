package com.jinmifood.jinmi.email.repository;

import com.jinmifood.jinmi.email.domain.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 이메일 인증 정보를 찾습니다.
    Optional<EmailVerification> findByEmail(String email);

    // 이메일을 기준으로 인증 정보를 삭제 (인증 성공시)
    void deleteByEmail(String email);

    // 이메일 존재 여부 확인 (코드 재발송시)
    boolean existsByEmail(String email);

    @Modifying // UPDATE나 DELETE 쿼리임을 명시
    @Transactional // 삭제는 트랜잭션 내에서 이루어져야 함
    @Query("DELETE FROM EmailVerification e WHERE e.createdAt < :time")
    int deleteByCreatedAtBefore(LocalDateTime time);
}
