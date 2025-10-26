package com.jinmifood.jinmi.user.dto.response;


import com.jinmifood.jinmi.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyInfoResponse {

    private Long userId;
    private String email;
    private String displayName;
    private String phoneNumber;
    private String address;
    private String provider;

    public static MyInfoResponse from(User user){
        return new MyInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getProvider()
        );
    }
}
