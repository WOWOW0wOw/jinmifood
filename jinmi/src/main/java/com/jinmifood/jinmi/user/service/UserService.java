package com.jinmifood.jinmi.user.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.common.security.JwtTokenProvider;
import com.jinmifood.jinmi.common.security.refreshToken.domain.BlacklistToken;
import com.jinmifood.jinmi.common.security.refreshToken.domain.RefreshToken;
import com.jinmifood.jinmi.common.security.refreshToken.repository.BlacklistTokenRepository;
import com.jinmifood.jinmi.common.security.refreshToken.repository.RefreshTokenRepository;
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

        // 이메일 중복확인
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new CustomException(ErrorException.DUPLICATE_EMAIL);
        }
        // 핸드폰번호 중복확인
        if(userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            throw new CustomException(ErrorException.DUPLICATE_PHONENUMBER);
        }
        // 닉네임 중복확인
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
    /**
     * 사용자 로그인 인증을 수행하고 JWT Access Token을 생성합니다.
     * @param request 로그인 요청 정보 (이메일, 비밀번호)
     * @return 생성된 JWT Access Token
     * @throws CustomException 인증 실패 시 (이메일 또는 비밀번호 불일치)
     */
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
        // ⭐️ 수정: generateToken -> generateAccessToken
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

    // 회원탈퇴 로직
    @Transactional
    public void deleteUser(Long userId, String userIdentifier, String accessToken) {
        log.info("회원 삭제 시작: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자 찾기 실패: userId={}", userId);
                    return new CustomException(ErrorException.NOT_FOUND);
                });

        log.info("사용자 정보 확인 완료: Email={}", user.getEmail());

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

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("현재 비밀번호 불일치: userId={}", userId);
            throw new CustomException(ErrorException.PASSWORD_MISMATCH);
        }
        String newDisplayName = request.getDisplayName();
        if (newDisplayName != null && !newDisplayName.equals(user.getDisplayName())) {
            if(userRepository.existsByDisplayName(newDisplayName)) {
                log.warn("닉네임 중복: userId={}, newDisplayName={}", userId, newDisplayName);
                throw new CustomException(ErrorException.DUPLICATE_NICKNAME);
            }
        }

        String newPhoneNumber = request.getPhoneNumber();
        if (newPhoneNumber != null && !newPhoneNumber.equals(user.getPhoneNumber())) {
            Optional<User> existingUser = userRepository.findByPhoneNumber(newPhoneNumber);

            if (existingUser.isPresent()) {
                log.warn("전화번호 중복: userId={}, newPhoneNumber={}", userId, newPhoneNumber);
                throw new CustomException(ErrorException.DUPLICATE_PHONENUMBER);
            }
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
                request.getPhoneNumber(),
                request.getAddress()
        );
        log.info("기타 정보 수정완료: userId={}", userId);
    }
}
