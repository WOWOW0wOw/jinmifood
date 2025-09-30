package com.jinmifood.jinmi.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@AllArgsConstructor
public enum ErrorException {

    DUPLICATE_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,"이미 사용중인 닉네임입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"요청한 대상을 찾을 수 없습니다."),
    DUPLICATE_CART(HttpStatus.CONFLICT, "이미 장바구니에 있습니다."),
    FULL_CART(HttpStatus.CONFLICT, "장바구니가 이미 찼습니다");

    private final HttpStatus httpStatus;
    private final String detail;

    public List<String> getDetail() {
        return List.of(this.detail);
    }

}
