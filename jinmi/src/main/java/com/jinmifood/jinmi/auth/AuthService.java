package com.jinmifood.jinmi.auth;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.security.refreshToken.domain.RefreshToken;
import com.jinmifood.jinmi.common.security.refreshToken.repository.RefreshTokenRepository;
import com.jinmifood.jinmi.user.dto.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse reissue(String existingRefreshToken) {

        System.out.println("Reissue 요청 Refresh Token: " + existingRefreshToken); // 기존 로그 유지

        // 1. Refresh Token 유효성 검사 (만료, 서명 등)
        if (!jwtTokenProvider.validateToken(existingRefreshToken)) {
            // 만료된 토큰이 넘어왔다면, 해당 토큰이 DB에 있는지 확인하고 삭제 (보안 강화)
            String expiredUserIdentifier = jwtTokenProvider.getUserIdentifier(existingRefreshToken);
            refreshTokenRepository.deleteById(expiredUserIdentifier);
            log.info("만료된 Refresh Token DB에서 삭제 완료: userIdentifier={}", expiredUserIdentifier);

            throw new CustomException(ErrorException.INVALID_REFRESH_TOKEN); // 401 Unauthorized
        }

        // 2. Refresh Token에서 사용자 식별자 추출 (DB 조회 기준)
        String userIdentifier = jwtTokenProvider.getUserIdentifier(existingRefreshToken);

        // 3. DB에서 사용자 식별자로 저장된 Refresh Token 정보 조회
        RefreshToken storedToken = refreshTokenRepository.findById(userIdentifier)
                .orElseThrow(() -> {
                    // DB에 토큰이 없음 (이미 갱신/로그아웃되었거나 Race Condition으로 다른 스레드가 삭제함)
                    log.warn("DB에 토큰 정보가 존재하지 않습니다: userIdentifier={}", userIdentifier);
                    return new CustomException(ErrorException.TOKEN_NOT_FOUND); // 404 Not Found
                });

        if (!storedToken.getTokenValue().equals(existingRefreshToken)) {
            refreshTokenRepository.deleteById(userIdentifier);
            log.error("Refresh Token 불일치 감지. DB 토큰 삭제: userIdentifier={}", userIdentifier);
            throw new CustomException(ErrorException.INVALID_REFRESH_TOKEN); // 401 Unauthorized
        }

        refreshTokenRepository.deleteById(userIdentifier);
        log.info("경쟁 조건 방지를 위해 기존 Refresh Token 즉시 삭제 완료: userIdentifier={}", userIdentifier);

        // 6. 새로운 Access Token & Refresh Token 생성
        Authentication authentication = jwtTokenProvider.getAuthentication(existingRefreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateTime(newRefreshToken);

        RefreshToken newStoredToken = RefreshToken.builder()
                .userIdentifier(userIdentifier)
                .tokenValue(newRefreshToken)
                .expiryDate(expiryDate)
                .build();
        refreshTokenRepository.save(newStoredToken);

        log.info("토큰 재발급 성공 및 새 토큰 저장 완료: userIdentifier={}", userIdentifier);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}