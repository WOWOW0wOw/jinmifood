package com.jinmifood.jinmi.user.dto.request;


import com.jinmifood.jinmi.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JoinUserRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
    private String address;
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String displayName; // 닉네임
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^[0-9]+$", message = "전화번호는 숫자만 입력 가능합니다.")
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
