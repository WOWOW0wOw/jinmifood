// com.jinmifood.jinmi.common.security.JwtAuthenticationEntryPoint.java
package com.jinmifood.jinmi.common.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        int status = HttpServletResponse.SC_UNAUTHORIZED; // 기본 상태는 401
        String errorMessage = "유효하지 않거나 토큰이 존재하지 않습니다."; // JWT 에러 메시지
        String errorCode = "UNAUTHORIZED"; // 메시지에 사용할 코드

        // 이 EntryPoint는 JWT 토큰 인증 실패만 담당하도록 로그인 경로는 무시합니다.

        response.setStatus(status);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // JSON 형식은 현재 사용하시는 형태를 따릅니다.
        String jsonError = String.format("{\"status\": %d, \"message\": \"%s\", \"errors\": [\"%s\"]}",
                status,
                errorCode,
                errorMessage);

        PrintWriter writer = response.getWriter();
        writer.write(jsonError);
        writer.flush();
    }
}