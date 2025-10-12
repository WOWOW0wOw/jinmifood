package com.jinmifood.jinmi.email.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.email.domain.EmailVerification;
import com.jinmifood.jinmi.email.repository.EmailVerificationRepository;
import com.jinmifood.jinmi.user.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
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

    private static final long AUTH_CODE_EXPIRATION_MINUTES = 5; // 인증 코드 유효시간 5분
    private static final long CLEANUP_DELAY = 10;


    private String createAuthCode(){
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000; //100000 ~ 999999
        return String.valueOf(code);
    }

    // 1단계 인증코드 생성, DB에 저장/갱신후, 이메일 발송

    @Transactional
    public void sendAuthMail(String email){
        if(userRepository.existsByEmail(email)){
            throw new CustomException(ErrorException.DUPLICATE_EMAIL);
        }

        String authCode = createAuthCode();
        Optional<EmailVerification> existingVerification = verificationRepository.findByEmail(email);

        // 2. DB에 저장 또는 갱신
        if (existingVerification.isPresent()) {
            // 이미 요청한 기록이 있으면 코드만 갱신 (재발송)
            EmailVerification verification = existingVerification.get();
            verification.updateCode(authCode);
            // Save는 JPA Dirty Checking으로 자동 처리되거나, 명시적으로 save(verification) 호출
        } else {
            // 새로운 요청이면 새로 생성
            EmailVerification newVerification = EmailVerification.builder()
                    .email(email)
                    .code(authCode)
                    .build();
            verificationRepository.save(newVerification);
        }
        // 3. 메일 내용 구성 및 발송
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("audqh656@gmail.com","진미푸드");
            helper.setTo(email);
            helper.setSubject("JinMiFood 회원가입 이메일 인증 코드");

            // HTML 메일 본문 구성 (인증 코드를 강조하여 전달)
            String htmlContent = "<html>"
                    + "<body style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<h3 style='color: #5cb85c;'>JinMiFood 회원가입 인증 안내</h3>"
                    + "<p>안녕하세요. 회원가입을 위한 인증 코드를 요청하셨습니다.</p>"
                    + "<p>아래 6자리 인증 코드를 인증 창에 입력해 주세요.</p>"
                    + "<div style='text-align: center; margin: 20px 0; padding: 15px; background-color: #f5f5f5; border-radius: 5px;'>"
                    + "<strong style='font-size: 24px; color: #d9534f;'>" + authCode + "</strong>" // 인증 코드 강조
                    + "</div>"
                    + "<p>이 코드는 <strong>" + AUTH_CODE_EXPIRATION_MINUTES + "분</strong> 동안 유효합니다.</p>"
                    + "<p>감사합니다.</p>"
                    + "</body>"
                    + "</html>";

            // true: HTML 형식으로 전송
            helper.setText(htmlContent, true);

            // 4. 메일 발송
            mailSender.send(mimeMessage);
            log.info("인증 메일 발송 성공: {}", email);
        } catch (Exception e) {
            log.error("인증 메일 발송 실패: {}", email, e);
            throw new CustomException(ErrorException.EMAIL_SEND_FAIL);
        }
    }
    // 2단계: 사용자가 입력한 인증 코드를 DB의 값과 비교하여 검증합니다.
    @Transactional
    public boolean verifyAuthCode(String email, String code) {
        // 1. DB에서 인증 정보 조회
        EmailVerification verification = verificationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorException.INVALID_AUTH_CODE));

        // 2. 만료 시간 확인
        LocalDateTime expirationTime = verification.getCreatedAt().plusMinutes(AUTH_CODE_EXPIRATION_MINUTES);
        if (LocalDateTime.now().isAfter(expirationTime)) {
            // 만료 시 DB에서 삭제 후 에러 반환
            verificationRepository.deleteByEmail(email);
            throw new CustomException(ErrorException.EXPIRED_AUTH_CODE);
        }

        // 3. 코드가 일치하는지 확인
        if (!verification.getCode().equals(code)) {
            throw new CustomException(ErrorException.INVALID_AUTH_CODE);
        }

        // 4. 인증 성공 시, DB에서 해당 인증 정보 삭제 (일회성 인증)
        verificationRepository.deleteByEmail(email);

        return true;
    }

    @Scheduled(fixedRate = CLEANUP_DELAY * 60 * 1000)
    public void cleanupExpiredAuthCodes() {
        // 인증 코드 유효시간이 지난 시점을 계산
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(AUTH_CODE_EXPIRATION_MINUTES);

        try {
            // 리포지토리를 통해 삭제 쿼리 실행
            int deletedCount = verificationRepository.deleteByCreatedAtBefore(cutoffTime);

            if (deletedCount > 0) {
                log.info("정기 정리: 만료된 인증 코드 {}개를 DB에서 정리했습니다.", deletedCount);
            }
        } catch (Exception e) {
            log.error("만료된 인증 코드 정리 중 오류 발생", e);
        }
    }

}
