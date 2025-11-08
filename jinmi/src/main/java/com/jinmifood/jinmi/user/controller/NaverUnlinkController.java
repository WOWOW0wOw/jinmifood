package com.jinmifood.jinmi.user.controller;

import com.jinmifood.jinmi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NaverUnlinkController {

    private final UserService userService;

    @PostMapping("/naver/unlink")
    public ResponseEntity<?> handleNaverUnlinkCallback(
            @RequestParam("client_id") String clientId,
            @RequestParam("service_provider") String serviceProvider,
            @RequestParam("user_id") String naverId
            // 네이버는 user_id(네이버 고유 ID)를 보내줍니다.
    ) {
        log.info("Naver 연결 끊기 Callback 수신: naverId={}", naverId);

        if ("NAVER".equalsIgnoreCase(serviceProvider)) {
            userService.handleNaverExternalUnlink(naverId);
            return ResponseEntity.ok().build();
        }

        log.warn("알 수 없는 Service Provider로부터 Naver Unlink Callback 수신: {}", serviceProvider);
        return ResponseEntity.badRequest().build();
    }
}