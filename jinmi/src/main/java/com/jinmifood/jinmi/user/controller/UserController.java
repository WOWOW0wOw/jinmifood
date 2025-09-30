package com.jinmifood.jinmi.user.controller;


import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.user.dto.request.JoinUserRequest;
import com.jinmifood.jinmi.user.dto.request.LoginUserRequest;
import com.jinmifood.jinmi.user.dto.response.JoinUserResponse;
import com.jinmifood.jinmi.user.dto.response.TokenResponse;
import com.jinmifood.jinmi.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/join")
    public StatusResponseDTO joinUser(@Valid @RequestBody JoinUserRequest request) {
        JoinUserResponse response = userService.registerUser(request);
        return StatusResponseDTO.ok( response.getUserId() + "님 회원가입 완료");
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginUserRequest request) {
        TokenResponse token = userService.login(request);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token.getAccessToken())
                .body(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<StatusResponseDTO> logout(HttpServletRequest request, Authentication authentication) {

        String accessToken = jwtTokenProvider.resolveToken(request);
        //로그인된 사용자 식별정보 가져오가

        String userIdentifier = jwtTokenProvider.getAuthentication(accessToken).getName();

        userService.logout(accessToken,userIdentifier);

        return ResponseEntity.ok(StatusResponseDTO.ok());

    }

}
