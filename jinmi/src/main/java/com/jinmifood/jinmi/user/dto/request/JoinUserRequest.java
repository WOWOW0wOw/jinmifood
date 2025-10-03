package com.jinmifood.jinmi.user.dto.request;


import com.jinmifood.jinmi.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
            message = "비밀번호는 영문 대소문자, 숫자를 포함한 8~20자리여야 합니다.")
    private String password;
    private String address;
    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 15, message = "표시 이름을 2자 이상 15자 이하로 입력해 주세요.")
    private String displayName; // 닉네임
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",
            message = "유효한 휴대폰 번호 형식이 아닙니다.")
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
