package com.jinmifood.jinmi.email.controller;

import com.jinmifood.jinmi.common.statusResponse.StatusResponseDTO;
import com.jinmifood.jinmi.email.dto.request.EmailRequest;
import com.jinmifood.jinmi.email.dto.request.VerificationRequest;
import com.jinmifood.jinmi.email.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class MailController {

    private final MailService mailService;

    @PostMapping("/send")
    public StatusResponseDTO sendAuthCode(@Valid @RequestBody EmailRequest request) {
        mailService.sendAuthMail(request.email());
        return StatusResponseDTO.ok("인증 코드가 이메일로 발송되었습니다. (유효시간 5분)");
    }

    // 2단계: 인증 코드 검증
    @PostMapping("/verify")
    public StatusResponseDTO verifyAuthCode(@Valid @RequestBody VerificationRequest request) {
        mailService.verifyAuthCode(request.email(), request.code());
        return StatusResponseDTO.ok("이메일 인증이 완료되었습니다.");
    }

}
