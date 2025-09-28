package com.jinmifood.jinmi.user.dto.response;


import com.jinmifood.jinmi.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinUserResponse {

    private final Long userId;
    private final String email;
    private final String displayName;

    public static JoinUserResponse from(User user) {
        return new JoinUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}
