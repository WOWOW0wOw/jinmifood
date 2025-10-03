package com.jinmifood.jinmi.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMyInfoRequest {

    @NotBlank(message = "현재 비밀번호는 필수 입력 항목입니다.")
    private String currentPassword;

//    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$",
//            message = "비밀번호는 영문 대소문자, 숫자를 포함한 8~20자리여야 합니다.")
    private String newPassword;

    @Size(min = 2, max = 15,
            message = "표시 이름을 2자 이상 15자 이하로 입력해 주세요.")
    private String displayName; // 닉네임
    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$",
            message = "유효한 휴대폰 번호 형식이 아닙니다.")
    private String phoneNumber;

    private String address;

    // 비밀번호 변경 요청이 있는지 확인
    public boolean isPasswordChangeRequested(){
        return this.newPassword != null && !this.newPassword.trim().isEmpty();
    }


}
