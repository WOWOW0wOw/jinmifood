package com.jinmifood.jinmi.config;

import com.jinmifood.jinmi.common.security.AccessLogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {
    private final AccessLogInterceptor accessLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**") // 모든 경로의 요청을 가로채서 로그 기록
                .excludePathPatterns(
                        "/api/v1/admin/access-logs", // 로그 조회 API 자체는 기록에서 제외
                        "/assets/**",                // 정적 리소스 제외
                        "/favicon.ico",
                        "/error"
                );
    }

}
