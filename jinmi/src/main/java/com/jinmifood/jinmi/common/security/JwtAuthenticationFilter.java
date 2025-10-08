package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.common.security.refreshToken.repository.BlacklistTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
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

            //  Access Token이 블랙리스트에 있는지 확인 (로그아웃된 토큰 검사)
            if (blacklistTokenRepository.existsById(accessToken)) {
                log.warn("블랙리스트 토큰 접근 시도. 토큰: {}", accessToken.substring(0, 10) + "...");
                // Spring Security Filter Chain을 통과하지 않도록 401 응답 후 종료
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Logged out token.");
                return;
            }

            //  토큰 유효성 검증 (validateToken은 만료 시 false를 반환해야 함)
            //  JwtTokenProvider.validateToken에서 예외를 던지지 않고, 만료 시 false를 반환하도록 수정했다는 전제
            if (jwtTokenProvider.validateToken(accessToken)) {
                log.info("Access Token 유효성 검사 성공: {}", accessToken.substring(0, 10) + "...");

                //  토큰이 유효하면 SecurityContext에 인증 정보 저장
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }else{
                //  토큰이 만료되거나 유효하지 않으면, 인증 정보는 설정하지 않고 다음 필터로 진행
                log.debug("Access Token 유효성 검사 실패. (만료 또는 서명 오류). 토큰: {}", accessToken.substring(0, 10) + "...");
            }
        } else{
            log.debug("요청 헤더에서 Access Token을 찾을 수 없음.");
        }

        // 인증 정보가 설정되지 않은 상태로 다음 필터 체인 진행
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