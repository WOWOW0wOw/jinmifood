package com.jinmifood.jinmi.common.security.refreshToken.repository;

import com.jinmifood.jinmi.common.security.refreshToken.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenValue(String tokenValue);
}
