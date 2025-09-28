package com.jinmifood.jinmi.user.dto.request;


import com.jinmifood.jinmi.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JoinUserRequest {

    private String email;
    private String password;
    private String address;
    private String displayName; // 닉네임
    private String phoneNumber;

    public User toEntity(String encodedPassword){
        return User.builder()
                .email(this.email)
                .password(encodedPassword)//암호화된 패스워스 사용
                .address(this.address)
                .displayName(this.displayName)
                .phoneNumber(this.phoneNumber)
                .build();
        // totalOrderCnt, createAt, role 등등 @PrePersist가 처리
    }

}
