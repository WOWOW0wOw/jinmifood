package com.jinmifood.jinmi.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private static final String AUTHORITIES_KEY = "auth";
    private final long accessTokenExpireTime;
    private final long refreshTokenExpireTime;
    private final CustomUserDetailsService customUserDetailsService;


    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-token-expiration-time}") long accessTokenExpireTime,
                            @Value("${jwt.refresh-token-expiration-time}") long refreshTokenExpireTime,
                            CustomUserDetailsService customUserDetailsService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpireTime = accessTokenExpireTime;
        this.refreshTokenExpireTime = refreshTokenExpireTime;
        this .customUserDetailsService = customUserDetailsService;
        log.info("JWT ACCESS TOKEN EXPIRE TIME: {} ms ({} 분)", accessTokenExpireTime, accessTokenExpireTime / 60000);
    }

    // 1. JWT Access Token 생성
    public String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenExpireTime);

        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return accessToken;
    }

    // JWT Refresh Token 생성
    public String generateRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date refreshTokenExpiresIn = new Date(now + refreshTokenExpireTime);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 2. JWT 토큰에서 인증 정보 조회 (Authentication 객체 생성)
    public Authentication getAuthentication(String token) {
        // 토큰에서 클레임(정보) 추출
        Claims claims = parseClaims(token);
        String userIdentifier = claims.getSubject();

        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities;

        // 권한 클레임(claims.get("auth"))이 존재하는지 확인
        if (claims.get(AUTHORITIES_KEY) != null) { // AUTHORITIES_KEY 사용
            String authClaims = claims.get(AUTHORITIES_KEY).toString();

            // Access Token인 경우 권한 정보를 가져와 설정
            if (StringUtils.hasText(authClaims)) { // 권한 문자열이 유효한지 확인
                authorities = Arrays.stream(authClaims.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText) //  빈 권한 필터링
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else {
                authorities = Collections.emptyList();
            }

        } else {
            // Refresh Token이거나 'auth' 클레임이 없는 경우 (권한 정보 없이 생성)
            authorities = Collections.emptyList();
        }

        // authorities가 비어있다면 (Refresh Token이거나 Access Token에 권한이 누락된 경우)
        // Spring Security의 기본 'ROLE_ANONYMOUS' 대신 특정 역할 부여
        if (authorities.isEmpty() && claims.get(AUTHORITIES_KEY) == null) {
            // Refresh Token처럼 처리: 권한 없이 사용자 정보만 로드
        } else if (authorities.isEmpty() && claims.get(AUTHORITIES_KEY) != null) {
            // Access Token인데 권한 추출에 실패했을 경우: 기본 권한 부여 (예: ROLE_USER)
            authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }


        // Refresh Token의 Subject(사용자 ID/이메일)만 사용합니다.
        UserDetails principal = customUserDetailsService.loadUserByUsername(userIdentifier);

        //  만약 Refresh Token이고 authorities가 비어있다면, principal에서 authorities를 가져오지 않도록
        // 아래와 같이 수정합니다. (현재 코드는 principal에서 authorities를 가져오지 않고 그대로 사용하므로 괜찮습니다.)
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 3. 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다.(필터에서 처리됨)", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 4. JWT 복호화 및 클레임 추출
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료되었어도 클레임은 반환
        }
    }

    public String getUserIdentifier(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰의 남은 유효 기간을 반환 (블랙리스트용)
    public long getExpireTime(String token) {
        try{
            Date expiration = parseClaims(token).getExpiration();
            long now = (new Date()).getTime();

            return Math.max(0, (expiration.getTime() - now) / 1000);
        }catch (Exception e) {
            log.error("토큰 만료 시간 계산 중 오류 발생", e);
            return 0;
        }
    }

    // 토큰의 만료 시각 (LocalDateTime)을 반환 (DB저장)

    public LocalDateTime getExpiryDateTime(String token) {
        Date expiration = parseClaims(token).getExpiration();

        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }


}