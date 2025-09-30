package com.jinmifood.jinmi.auth;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.security.refreshToken.domain.RefreshToken;
import com.jinmifood.jinmi.common.security.refreshToken.repository.RefreshTokenRepository;
import com.jinmifood.jinmi.user.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse reissue(String refreshToken) {

        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorException.INVALID_REFRESH_TOKEN);
        }
        RefreshToken storedToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorException.TOKEN_NOT_FOUND));

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateTime(newRefreshToken);
        storedToken.updateToken(newRefreshToken, expiryDate);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
