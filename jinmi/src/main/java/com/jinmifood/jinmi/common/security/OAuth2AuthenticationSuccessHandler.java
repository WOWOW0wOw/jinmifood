package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.common.security.refreshToken.domain.RefreshToken;
import com.jinmifood.jinmi.common.security.refreshToken.repository.RefreshTokenRepository;
import com.jinmifood.jinmi.user.domain.User;
import com.jinmifood.jinmi.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final String REDIRECT_URI = "http://localhost:5173/oauth2/redirect";
    private final UserRepository userRepository;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String googleAccessToken = null;

        if (oauthToken.getAuthorizedClientRegistrationId().equals("google")) {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (authorizedClient != null) {
                googleAccessToken = authorizedClient.getAccessToken().getTokenValue();
                log.info("Google Access Token 추출 완료 (Revoke 대상): {}", googleAccessToken);
            } else {
                log.warn("OAuth2AuthorizedClient를 찾을 수 없습니다. Google Access Token 추출 실패.");
            }
        }

        // 1. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        String userIdentifier = userDetails.getUsername(); // 이메일
        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateTime(refreshToken);

        // 2. Refresh Token DB 저장 또는 업데이트 (기존 로그인 로직 재활용)
        refreshTokenRepository.findById(userIdentifier)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken, expiryDate),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userIdentifier(userIdentifier)
                                .tokenValue(refreshToken)
                                .expiryDate(expiryDate)
                                .build())
                );
        log.info("OAuth2 로그인 성공: JWT 발행 및 Refresh Token 저장 완료 (사용자: {})", userIdentifier);

        if (googleAccessToken != null) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login"));

            user.updateGoogleRefreshToken(googleAccessToken); // 메서드 이름을 googleAccessToken으로 변경하는 것도 고려
            userRepository.save(user); // DB에 Google 토큰 저장
            log.info("Google Access Token 저장 완료: {}", googleAccessToken);
        }

        // 3. 프론트엔드로 리다이렉트할 URI 빌드 (토큰 전달)
        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 4. 최종 리다이렉션 수행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
