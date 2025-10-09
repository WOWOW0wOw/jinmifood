package com.jinmifood.jinmi.user.controller;


import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.CustomUserDetails;
import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.user.dto.request.JoinUserRequest;
import com.jinmifood.jinmi.user.dto.request.LoginUserRequest;
import com.jinmifood.jinmi.user.dto.request.UpdateMyInfoRequest;
import com.jinmifood.jinmi.user.dto.response.JoinUserResponse;
import com.jinmifood.jinmi.user.dto.response.MyInfoResponse;
import com.jinmifood.jinmi.user.dto.response.TokenResponse;
import com.jinmifood.jinmi.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;


    //회원가입
    @PostMapping("/join")
    public StatusResponseDTO joinUser(@Valid @RequestBody JoinUserRequest request) {
        JoinUserResponse response = userService.registerUser(request);

        return StatusResponseDTO.ok(response);
    }


    // 로그인 및 토큰 발급
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> loginUser(@RequestBody LoginUserRequest request) {
        TokenResponse token = userService.login(request);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token.getAccessToken())
                .body(token);
    }

    // 로그아웃
    @PostMapping("/logout")
    public StatusResponseDTO logout(HttpServletRequest request, Authentication authentication) {

        String accessToken = jwtTokenProvider.resolveToken(request);
        //로그인된 사용자 식별정보 가져오가

        String userIdentifier = jwtTokenProvider.getAuthentication(accessToken).getName();

        userService.logout(accessToken,userIdentifier);

        return StatusResponseDTO.ok("로그아웃 완료");

    }

    @DeleteMapping("/delete")
    public StatusResponseDTO delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        if (userDetails == null) {
            //️ NullPointerException을 CustomException으로 대체하여 응답
            log.error("@AuthenticationPrincipal userDetails is NULL. 인증 정보가 SecurityContext에 없습니다.");
            throw new CustomException(ErrorException.INVALID_ACCESS_TOKEN); // 401 에러를 던지도록 수정
        }
        Long userId = userDetails.getId();

        log.info("Controller: User ID({}) 확인 완료. Access Token 추출 시작.", userId);
        String accessToken = jwtTokenProvider.resolveToken(request);

        log.info("Controller: Access Token 추출 완료. 서비스 호출 시작.");


        String userIdentifier = jwtTokenProvider.getAuthentication(accessToken).getName();
        userService.deleteUser(userId,userIdentifier,accessToken);
        log.info("Controller: 서비스 호출 완료. 삭제 성공 응답.");

        return StatusResponseDTO.ok("회원 탈퇴 성공");

    }
    // 내 정보 불러오기
    @GetMapping("/myInfo")
    public StatusResponseDTO getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getId();

        MyInfoResponse response = userService.getMyinfo(userId);

        return StatusResponseDTO.ok(response);
    }
    // 내정보 수정하기
    @PutMapping("/myUpdateInfo")
    public StatusResponseDTO updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateMyInfoRequest request) {

        Long userId = userDetails.getId();

        userService.updateMyInfo(userId,request);
        return StatusResponseDTO.ok("회원정보가 수정 성공");

    }

    @GetMapping("/checkNickname")
    public StatusResponseDTO checkNickname(@RequestParam("displayName") String nickname) {

        if(userService.existsByDisplayName(nickname)){
            log.warn("닉네임 중복 확인: {} 닉네임이 이미 존재합니다.", nickname);
            throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
        }

        log.info("닉네임 중복 확인: {} 닉네임 사용 가능", nickname);
        return StatusResponseDTO.ok("사용 가능한 닉네임 입니다.");

    }
}
