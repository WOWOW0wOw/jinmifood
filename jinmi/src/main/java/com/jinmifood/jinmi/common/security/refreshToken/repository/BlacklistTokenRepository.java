package com.jinmifood.jinmi.common.security.refreshToken.repository;

import com.jinmifood.jinmi.common.security.refreshToken.domain.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, String> {
    // existsById(String accessToken) 를 사용하여 토큰 존재 유무 확인
}
