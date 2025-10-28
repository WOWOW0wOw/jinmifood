package com.jinmifood.jinmi.user.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.security.refreshToken.domain.BlacklistToken;
import com.jinmifood.jinmi.common.security.refreshToken.domain.RefreshToken;
import com.jinmifood.jinmi.common.security.refreshToken.repository.BlacklistTokenRepository;
import com.jinmifood.jinmi.common.security.refreshToken.repository.RefreshTokenRepository;
import com.jinmifood.jinmi.email.service.MailService;
import com.jinmifood.jinmi.user.domain.User;
import com.jinmifood.jinmi.user.dto.request.JoinUserRequest;
import com.jinmifood.jinmi.user.dto.request.LoginUserRequest;
import com.jinmifood.jinmi.user.dto.request.UpdateMyInfoRequest;
import com.jinmifood.jinmi.user.dto.response.JoinUserResponse;
import com.jinmifood.jinmi.user.dto.response.MyInfoResponse;
import com.jinmifood.jinmi.user.dto.response.TokenResponse;
import com.jinmifood.jinmi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import com.jinmifood.jinmi.common.constant.ReservedKeywords;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistTokenRepository blacklistTokenRepository;
    private final MailService mailService;
    private final WebClient webClient;


    private boolean isReservedKeywordUsed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        final String lowerCaseText = text.toLowerCase();

        return ReservedKeywords.RESERVED_LIST.stream()
                .anyMatch(keyword -> lowerCaseText.contains(keyword));
    }

    // 이메일 찾기
    @Transactional(readOnly = true)
    public User findByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorException.NOT_FOUND)); //404
    }

    //닉네임 존재여부 확인(나중에 닉네임수정할 때 쓰기)
    @Transactional(readOnly = true)
    public boolean existsByDisplayName(String nickname){
        return userRepository.existsByDisplayName(nickname);

    }


    // 가입
    @Transactional
    public JoinUserResponse registerUser(JoinUserRequest user) {

        if (isReservedKeywordUsed(user.getDisplayName())) {
            throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
        }

        String emailId = user.getEmail().split("@")[0];
        if (isReservedKeywordUsed(emailId)) {
            throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
        }


        // 이메일 중복확인
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new CustomException(ErrorException.DUPLICATE_EMAIL);
        }
        // 핸드폰번호 중복확인
        if(userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new CustomException(ErrorException.DUPLICATE_PHONENUMBER);
        }
        // 닉네임 중복확인 (일반적인 중복 확인)
        if(userRepository.existsByDisplayName(user.getDisplayName())){
            throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
        }

        //비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(user.getPassword());

        User newUser = user.toEntity(encodedPassword);
        User savedUser = userRepository.save(newUser);
        return JoinUserResponse.from(savedUser);
    }

    // 로그인

    @Transactional(readOnly = false)
    public TokenResponse login(LoginUserRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(authenticationToken);

        } catch(Exception e){
            log.error("로그인 인증 실패: {}",e.getMessage());
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorException.NOT_FOUND));
        user.updateLastLoginAt();

        // accessToken && refreshToken 생성
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        String userIdentifier = authentication.getName();
        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateTime(refreshToken);

        refreshTokenRepository.findById(userIdentifier)
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken, expiryDate),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userIdentifier(userIdentifier)
                                .tokenValue(refreshToken)
                                .expiryDate(expiryDate)
                                .build())
                );
        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public void checkPassword(String email, String rawPassword) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, rawPassword);

        try{
            authenticationManager.authenticate(authenticationToken);
            log.info("비밀번호 확인 성공: Email={}", email);

        } catch(Exception e){
            log.error("비밀번호 불일치: Email={}", email);
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }
    }



    @Transactional
    public void logout(String accessToken, String userIdentifier) {

        // Refresh Token db에서 삭제 (재발급 막음)
        refreshTokenRepository.deleteById(userIdentifier);
        log.info("Refresh Token 삭제 완료 : 사용자 ID = {}", userIdentifier);

        Long remainingSeconds = jwtTokenProvider.getExpireTime(accessToken);

        if (remainingSeconds > 0) {

            LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(remainingSeconds);

            BlacklistToken blacklistToken = BlacklistToken.builder()
                    .accessToken(accessToken)
                    .userIdentifier(userIdentifier)
                    .expiryAt(expiredAt)
                    .build();
            blacklistTokenRepository.save(blacklistToken);
            log.info("Access Token 블랙리스트 추가 완료: 토큰 만료까지 {}초 남음", expiredAt);
        }
        log.info("로그아웃 성공: 사용자 ID = {}", userIdentifier);
    }

    private void revokeGoogleToken(String tokenValue) {
        final String GOOGLE_REVOKE_URL_TEMPLATE = "https://oauth2.googleapis.com/revoke?token={token}";

        try {
            webClient.post()
                    .uri(GOOGLE_REVOKE_URL_TEMPLATE, tokenValue)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Google Revoke 요청 전송 완료: Token={}", tokenValue);

        } catch (WebClientResponseException e) {
            log.error("Google Revoke API 호출 실패. 상태코드: {}, 응답 본문: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Google Revoke 처리 중 알 수 없는 오류 발생: {}", e.getMessage());
        }
    }

    // 회원탈퇴 로직
    @Transactional
    public void deleteUser(Long userId, String userIdentifier, String accessToken) {
        log.info("회원 삭제 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자 찾기 실패: userId={}", userId);
                    return new CustomException(ErrorException.NOT_FOUND);
                });

        boolean isSocialUser = user.getProvider() != null &&
                !user.getProvider().equals("NONE") &&
                !user.getProvider().equals("LOCAL");

        log.info("사용자 정보 확인 완료: Email={}", user.getEmail());

        if (isSocialUser && user.getProvider() != null && user.getProvider().equals("google")) {
            log.info("소셜 로그인 사용자입니다. Google 연결 해제를 시도합니다: userId={}", userId);

            String googleTokenToRevoke = user.getGoogleRefreshToken();

            if (googleTokenToRevoke != null) {
                revokeGoogleToken(googleTokenToRevoke);
                user.clearGoogleRefreshToken(); // 💡
                log.info("Google 연결 해제(Revoke) 완료: userId={}", userId);
            } else {
                log.warn("Google Refresh/Access Token을 찾을 수 없습니다. Google Revoke를 건너뜁니다: userId={}", userId);
            }
        }

        refreshTokenRepository.deleteById(userIdentifier);
        log.info("Refresh Token 삭제 완료 : 사용자 ID = {}", userIdentifier);

        Long remainningExpiration = jwtTokenProvider.getExpireTime(accessToken);

        if (remainningExpiration > 0) {
            LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(remainningExpiration);

            BlacklistToken blacklistToken = BlacklistToken.builder()
                    .accessToken(accessToken)
                    .userIdentifier(userIdentifier)
                    .expiryAt(expiredAt)
                    .build();
            blacklistTokenRepository.save(blacklistToken);
            log.info("Access Token 블랙리스트 추가 완료: 토큰 만료까지 {}초 남음", expiredAt);

        }

        log.info("토큰 무효화 로직 진행 완료");
        userRepository.delete(user);
        log.info("회원 탈퇴 및 계정 삭제 성공 ID = {} Email = {}",userId, userIdentifier);


    }


    public MyInfoResponse getMyinfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorException.NOT_FOUND));

        return MyInfoResponse.from(user);
    }

    @Transactional
    public void updateMyInfo(Long userId, UpdateMyInfoRequest request) {
        log.info("회원정보 수정 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자 정보를 찾을 수 없음: userId={}", userId);
                    return new CustomException(ErrorException.NOT_FOUND);
                });

        boolean isSocialUser = user.getProvider() != null &&
                !user.getProvider().equals("NONE") &&
                !user.getProvider().equals("LOCAL");

        if (!isSocialUser) {
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                log.warn("null이 들어옴");
            }

            // 현재 비밀번호 일치 여부 확인
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                log.warn("현재 비밀번호 불일치: userId={}", userId);
                throw new CustomException(ErrorException.PASSWORD_MISMATCH);
            }
        }

        String newDisplayName = request.getDisplayName();
        if (newDisplayName != null && !newDisplayName.equals(user.getDisplayName())) {

            if (isReservedKeywordUsed(newDisplayName)) {
                log.warn("닉네임 예약 키워드 사용 시도: userId={}, newDisplayName={}", userId, newDisplayName);
                throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
            }

            if(userRepository.existsByDisplayName(newDisplayName)) {
                log.warn("닉네임 중복: userId={}, newDisplayName={}", userId, newDisplayName);
                throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
            }
        }

        String newPhoneNumber = request.getPhoneNumber();
        String finalPhoneNumberToUpdate = user.getPhoneNumber();

        if (newPhoneNumber != null && !newPhoneNumber.equals(user.getPhoneNumber())) {

            String cleanedPhoneNumber = newPhoneNumber.replaceAll("-", "");
            String phonePattern = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$";

            if (!cleanedPhoneNumber.matches(phonePattern)) {
                log.warn("전화번호 패턴 불일치: userId={}, newPhoneNumber={}", userId, newPhoneNumber);
                throw new CustomException(ErrorException.PHONENUMBER_MISPATTERN);
            }

            Optional<User> existingUser = userRepository.findByPhoneNumber(cleanedPhoneNumber);

            if (existingUser.isPresent()) {
                log.warn("전화번호 중복: userId={}, cleanedPhoneNumber={}", userId, cleanedPhoneNumber);
                throw new CustomException(ErrorException.DUPLICATE_PHONENUMBER);
            }

            finalPhoneNumberToUpdate = cleanedPhoneNumber;
        }

        if (request.isPasswordChangeRequested()){
            String newPassword = request.getNewPassword();
            String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";

            if(!newPassword.matches(passwordPattern)){
                log.warn("새 비밀번호 패턴 불일치: userId={}", userId);
                throw new CustomException(ErrorException.PASSWORD_MISPATTERN);
            }
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            user.updatePassword(encodedNewPassword);
            log.info("비밀번호 변경완료: userId={}", userId);
        }

        user.updateDetails(
                request.getDisplayName(),
                finalPhoneNumberToUpdate,
                request.getAddress()
        );

        log.info("기타 정보 수정완료: userId={}", userId);
    }

    @Transactional
    public void sendIdVerificationCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorException.USER_NOT_FOUND);
        }

        mailService.sendFindIdMail(email);
    }
    @Transactional
    public String verifyIdCodeAndFindId(String email, String code) {
        if (mailService.verifyFindIdCode(email, code)) {
            return email;
        }
        throw new CustomException(ErrorException.INVALID_AUTH_CODE);
    }

    @Transactional
    public void sendPasswordVerificationCode(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorException.USER_NOT_FOUND);
        }

        mailService.sendFindPasswordMail(email);
    }

    @Transactional(readOnly = true)
    public void verifyPasswordCode(String email, String code) {
        if (!mailService.verifyFindPasswordCode(email, code)) {
            throw new CustomException(ErrorException.INVALID_AUTH_CODE);
        }
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        if (!mailService.verifyFindPasswordCode(email, code)) {
            throw new CustomException(ErrorException.INVALID_AUTH_CODE);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorException.USER_NOT_FOUND));

        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";
        if(!newPassword.matches(passwordPattern)){
            throw new CustomException(ErrorException.PASSWORD_MISPATTERN);
        }

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);
        userRepository.save(user);

        log.info("비밀번호 재설정 성공: Email={}", email);
    }



}