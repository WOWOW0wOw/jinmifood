package com.jinmifood.jinmi.user.service;

import com.jinmifood.jinmi.common.exception.CustomException;
import com.jinmifood.jinmi.common.exception.ErrorException;
import com.jinmifood.jinmi.user.domain.User;
import com.jinmifood.jinmi.user.dto.request.JoinUserRequest;
import com.jinmifood.jinmi.user.dto.response.JoinUserResponse;
import com.jinmifood.jinmi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

}
