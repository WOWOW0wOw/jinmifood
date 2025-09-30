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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final BlacklistTokenRepository blacklistTokenRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 기본 보안 옵션
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 사용 안 함 (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 인증 실패 처리
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider,blacklistTokenRepository), UsernamePasswordAuthenticationFilter.class)

                // 인가 규칙
                .authorizeHttpRequests(auth -> auth

                        // 회원가입 / 로그인

                        .requestMatchers(HttpMethod.POST, "/users/join", "/users/login", "/itemCart/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/users/join", "/users/login", "/auth/reissue", "/itemCart/**"
                                ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/users/logout").authenticated()


                        // ✅ Swagger/OpenAPI 문서 허용
                        .requestMatchers(
                                "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**",
                                "/scalar/**" // ✅ 추가
                        ).permitAll()
                        // 그 밖의 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
