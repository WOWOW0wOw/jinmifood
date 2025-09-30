package com.jinmifood.jinmi.auth;


import com.jinmifood.jinmi.user.dto.request.ReissueRequest;
import com.jinmifood.jinmi.user.dto.response.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        TokenResponse tokens = authService.reissue(request.getRefreshToken());

        // Access Token은 헤더에, 전체 토큰 쌍은 Body에 반환
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + tokens.getAccessToken())
                .body(tokens);
    }
}
