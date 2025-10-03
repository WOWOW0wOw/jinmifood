package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.common.security.refreshToken.repository.BlacklistTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final BlacklistTokenRepository blacklistTokenRepository; // 블랙리스트 확인용

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveToken(request);


        if (StringUtils.hasText(accessToken)) {

            // 1. Access Token이 블랙리스트에 있는지 확인 (로그아웃된 토큰 검사)
            if (blacklistTokenRepository.existsById(accessToken)) {
                log.warn("블랙리스트 토큰 접근 시도 401 반환 : {}", accessToken);
                // 401 Unauthorized 응답
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Logged out token.");
                return;
            }

            // 2. 토큰이 유효한지 검증 (기존 로직)
            if (jwtTokenProvider.validateToken(accessToken)) {
                log.info("Access Token 유효성 검사 성공: {}", accessToken.substring(0, 10) + "...");
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                log.warn("Access Token 유효성 검사 실패. 토큰: {}", accessToken.substring(0, 10) + "...");
            }
        } else{
            log.debug("요청 헤더에서 Access Token을 찾을 수 없음.");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }
        return null;
    }
}