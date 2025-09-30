package com.jinmifood.jinmi.common.security.refreshToken.repository;

import com.jinmifood.jinmi.common.security.refreshToken.domain.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, String> {
    // existsById(String accessToken) 를 사용하여 토큰 존재 유무 확인

    @Transactional
    @Modifying
    @Query("DELETE FROM BlacklistToken b WHERE b.expiryAt <= :now")
    int deleteExpiredTokens(@Param("now")LocalDateTime now);
}
