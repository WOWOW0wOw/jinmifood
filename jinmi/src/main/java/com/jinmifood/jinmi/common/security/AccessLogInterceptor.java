package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.user.service.AccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AccessLogInterceptor implements HandlerInterceptor {
    private final AccessLogService accessLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        // 정적 리소스 및 API 조회 요청은 로그 수집에서 제외
        if (!uri.contains("/assets/") && !uri.contains("/favicon") &&
                !uri.equals("/error") && !uri.contains("/api/v1/admin")) {
            accessLogService.saveLog(request);
        }
        return true;
    }
}
