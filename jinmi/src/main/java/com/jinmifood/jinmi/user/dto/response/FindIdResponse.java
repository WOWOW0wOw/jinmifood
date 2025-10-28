package com.jinmifood.jinmi.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FindIdResponse {
    private String email;
    private String provider;
}
