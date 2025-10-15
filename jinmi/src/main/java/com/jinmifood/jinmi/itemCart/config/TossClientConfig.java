package com.jinmifood.jinmi.itemCart.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
class TossClientConfig {
    @Value("${toss.secret-key}")
    private String secretKey;

    @Bean
    WebClient tossWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.tosspayments.com")
                // Basic auth: 아이디=secretKey, 비밀번호=""
                .defaultHeaders(h -> h.setBasicAuth(secretKey, ""))
                .build();
    }
}