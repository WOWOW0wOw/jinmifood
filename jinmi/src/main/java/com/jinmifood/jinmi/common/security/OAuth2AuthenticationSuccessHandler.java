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
import org.springframework.transaction.annotation.Transactional; // Import 추가
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
    @Transactional // @Transactional 추가
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String providerTokenToSave = null;

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                provider,
                oauthToken.getName()
        );

        if (authorizedClient != null) {
            if (provider.equals("google")) {
                providerTokenToSave = authorizedClient.getAccessToken().getTokenValue();
                log.info("Google Access Token 추출 완료 (Revoke 대상): {}", providerTokenToSave);
            } else if (provider.equals("kakao")) {
                if (authorizedClient.getRefreshToken() != null) {
                    providerTokenToSave = authorizedClient.getRefreshToken().getTokenValue();
                    log.info("Kakao Refresh Token 추출 완료 (저장 대상): {}", providerTokenToSave);
                } else {
                    log.warn("Kakao Refresh Token이 AuthorizedClientService에 저장되어 있지 않습니다.");
                }
            }
        } else {
            log.warn("OAuth2AuthorizedClient를 찾을 수 없습니다. 외부 토큰 추출 실패.");
        }

        // 1. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        String userIdentifier = userDetails.getUsername(); // 이메일
        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateTime(refreshToken);

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



        if (provider.equals("naver") || providerTokenToSave != null) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login"));

            if (provider.equals("google")) {
                user.updateGoogleRefreshToken(providerTokenToSave);
                log.info("Google Access Token 저장 완료: {}", providerTokenToSave);
            } else if (provider.equals("kakao")) {
                user.updateKakaoRefreshToken(providerTokenToSave);
                log.info("Kakao Refresh Token 저장 완료: {}", providerTokenToSave);
            } else if (provider.equals("naver")) {
                String naverAccessToken = authorizedClient.getAccessToken().getTokenValue();
                user.updateNaverAccessToken(naverAccessToken);
                log.info("Naver Access Token 저장 완료: User Email={}", user.getEmail());

                if (authorizedClient.getRefreshToken() != null) {
                    String naverRefreshToken = authorizedClient.getRefreshToken().getTokenValue();
                    user.updateNaverRefreshToken(naverRefreshToken);
                    log.info("Naver Refresh Token 저장 완료: User Email={}", user.getEmail());
                }
            }
        }


        String targetUrl = UriComponentsBuilder.fromUriString(REDIRECT_URI)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}