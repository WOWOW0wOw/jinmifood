package com.jinmifood.jinmi.email.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.email.domain.EmailVerification;
import com.jinmifood.jinmi.email.repository.EmailVerificationRepository;
import com.jinmifood.jinmi.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;

    private static final long AUTH_CODE_EXPIRATION_MINUTES = 5;
    private static final long CLEANUP_DELAY = 10;

    private String createAuthCode(){
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }


    private void sendVerificationEmail(String email, String subject) {
        String authCode = createAuthCode();
        Optional<EmailVerification> existingVerification = verificationRepository.findByEmail(email);

        if (existingVerification.isPresent()) {
            EmailVerification verification = existingVerification.get();
            verification.updateCode(authCode);
        } else {
            EmailVerification newVerification = EmailVerification.builder()
                    .email(email)
                    .code(authCode)
                    .build();
            verificationRepository.save(newVerification);
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("audqh656@gmail.com", "진미푸드");
            helper.setTo(email);
            helper.setSubject(subject);

            String htmlContent = "<html>"
                    + "<body style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<h3 style='color: #5cb85c;'>" + subject + " 안내</h3>"
                    + "<p>안녕하세요. 인증 코드를 요청하셨습니다.</p>"
                    + "<p>아래 6자리 인증 코드를 인증 창에 입력해 주세요.</p>"
                    + "<div style='text-align: center; margin: 20px 0; padding: 15px; background-color: #f5f5f5; border-radius: 5px;'>"
                    + "<strong style='font-size: 24px; color: #d9534f;'>" + authCode + "</strong>"
                    + "</div>"
                    + "<p>이 코드는 <strong>" + AUTH_CODE_EXPIRATION_MINUTES + "분</strong> 동안 유효합니다.</p>"
                    + "<p>감사합니다.</p>"
                    + "</body>"
                    + "</html>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            log.info("{} 메일 발송 성공: {}", subject, email);
        } catch (Exception e) {
            log.error("{} 메일 발송 실패: {}", subject, email, e);
            throw new CustomException(ErrorException.EMAIL_SEND_FAIL);
        }
    }


    @Transactional
    public boolean verifyAuthCode(String email, String code) {
        EmailVerification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorException.INVALID_AUTH_CODE));

        LocalDateTime expirationTime = verification.getCreatedAt().plusMinutes(AUTH_CODE_EXPIRATION_MINUTES);
        if (LocalDateTime.now().isAfter(expirationTime)) {
            // 만료 시에도 삭제 후 에러
            verificationRepository.deleteByEmail(email);
            throw new CustomException(ErrorException.EXPIRED_AUTH_CODE);
        }

        if (!verification.getCode().equals(code)) {
            throw new CustomException(ErrorException.INVALID_AUTH_CODE);
        }

        verificationRepository.deleteByEmail(email);
        log.info("인증 코드 확인 성공 및 DB에서 삭제 완료: {}", email);

        return true;
    }


    @Transactional
    public void sendAuthMail(String email){
        if(userRepository.existsByEmail(email)){
            throw new CustomException(ErrorException.DUPLICATE_EMAIL);
        }
        sendVerificationEmail(email, "JinMiFood 회원가입 이메일 인증 코드");
    }


    @Transactional
    public void sendFindIdMail(String email){
        sendVerificationEmail(email, "JinMiFood 아이디 찾기 인증 코드");
    }

    @Transactional
    public boolean verifyFindIdCode(String email, String code) {
        // 💡 재사용: 핵심 인증 로직(삭제 포함) 호출
        return verifyAuthCode(email, code);
    }


    @Transactional
    public void sendFindPasswordMail(String email){
        sendVerificationEmail(email, "JinMiFood 비밀번호 찾기 인증 코드");
    }

    @Transactional
    public boolean verifyFindPasswordCode(String email, String code) {
        return verifyAuthCode(email, code);
    }


    @Scheduled(fixedRate = CLEANUP_DELAY * 60 * 1000)
    public void cleanupExpiredAuthCodes() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(AUTH_CODE_EXPIRATION_MINUTES);

        try {
            int deletedCount = verificationRepository.deleteByCreatedAtBefore(cutoffTime);

            if (deletedCount > 0) {
                log.info("정기 정리: 만료된 인증 코드 {}개를 DB에서 정리했습니다.", deletedCount);
            }
        } catch (Exception e) {
            log.error("만료된 인증 코드 정리 중 오류 발생", e);
        }
    }
}