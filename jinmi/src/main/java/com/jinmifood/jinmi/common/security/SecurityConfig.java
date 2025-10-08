package com.jinmifood.jinmi.common.security;

import com.jinmifood.jinmi.common.security.refreshToken.repository.BlacklistTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final BlacklistTokenRepository blacklistTokenRepository;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, blacklistTokenRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // http://localhost:5173이 React 개발 서버의 기본 주소
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));

        // 모든 메서드 허용 (GET, POST, PUT, DELETE)
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 모든 헤더 허용 (Authorization 헤더 포함)
        config.setAllowedHeaders(List.of("*"));

        // 쿠키 및 인증 정보(jwt)를 포함한 요청 허용
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로 (/**)에 대해 위 설정 적용
        source.registerCorsConfiguration("/**", config);

        return source;

    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                // 기본 보안 옵션
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 사용 안 함 (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증 실패 처리
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth


                        //  인증 불필요 (permitAll) 경로를 URL 패턴으로 통합

                        .requestMatchers(
                                // 회원가입/로그인/토큰 재발급
                                "/users/join", "/users/login", "/auth/reissue",

                                // 장바구니 리스트 조회는 비회원도 가능하다고 가정
                                "/itemCart/list",

                                // 장바구니/주문 관련 API
                                "/itemCart/**", "/order/**",

                                // 상품 및 카테고리 추가
                                "/items/add", "/categories/add",

                                // Swagger/OpenAPI 문서
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/scalar/**"
                        ).permitAll()

                        //  인증 필요 (authenticated) 경로를 URL 패턴으로 통합
                        .requestMatchers(
                                // 내 정보 조회 및 수정
                                "/users/myInfo", "/users/myUpdateInfo",

                                // 로그아웃, 회원탈퇴
                                "/users/logout", "/users/delete"
                        ).authenticated()

                        // 그 밖의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
