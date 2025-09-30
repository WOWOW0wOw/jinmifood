package com.jinmifood.jinmi.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    // 클라이언트가 실제 API 요청에 사용할 짧은 만료 시간의 토큰
    private String accessToken;

    // Access Token이 만료되었을 때 재발급 요청에 사용할 긴 만료시간 토큰
    private String refreshToken;
}
