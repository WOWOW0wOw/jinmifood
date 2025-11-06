package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.user.service.CustomOAuth2UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final String FRONTEND_BASE_URL = "http://localhost:5173";
    private final String LOGIN_PAGE_PATH = "/login";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorCode = "UNKNOWN_OAUTH2_ERROR";

        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            errorCode = oauth2Exception.getError().getErrorCode();

            if (CustomOAuth2UserService.DUPLICATE_EMAIL_DIFFERENT_PROVIDER.equals(errorCode)) {
                log.warn("인증 실패 (이메일 충돌 감지): {}", oauth2Exception.getMessage());
            }
        } else {
            errorCode = "AUTHENTICATION_FAILED";
        }

        String encodedErrorCode = URLEncoder.encode(errorCode, StandardCharsets.UTF_8);
        String finalRedirectUrl = FRONTEND_BASE_URL + LOGIN_PAGE_PATH + "?error=" + encodedErrorCode;

        log.info("인증 실패 후 리다이렉트: {}", finalRedirectUrl);

        response.sendRedirect(finalRedirectUrl);
    }
}