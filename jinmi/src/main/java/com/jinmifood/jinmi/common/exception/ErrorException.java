package com.jinmifood.jinmi.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@AllArgsConstructor
public enum ErrorException {

    DUPLICATE_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    DUPLICATE_PHONENUMBER(HttpStatus.CONFLICT, "이미 사용중인 전화번호입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,"이미 사용중인 닉네임입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,"요청한 대상을 찾을 수 없습니다."),

    DUPLICATE_CART(HttpStatus.CONFLICT, "이미 장바구니에 있습니다."),
    FULL_CART(HttpStatus.CONFLICT, "장바구니가 이미 찼습니다"),

    QTY_NOTZERO(HttpStatus.CONFLICT, "최소 하나를 담아야합니다."),
    QTY_FULL(HttpStatus.CONFLICT, "최대 100개까지 담을 수 있습니다."),

    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "DB에 토큰 정보가 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않거나 만료된 Refresh Token입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED,"유효하지 않거나 만료된 Access Token 입니다."),
    PASSWORD_MISMATCH(HttpStatus.CONFLICT,"현재 비밀번호 불일치"),
    PASSWORD_MISPATTERN(HttpStatus.CONFLICT,"새 비밀번호 패턴 불일치"),

    DUPLICATE_ITEM_NAME(HttpStatus.CONFLICT, "이미 존재하는 아이템 이름입니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "이미 존재하는 카테고리 이름입니다.");


    private final HttpStatus httpStatus;
    private final String detail;

    public List<String> getDetail() {
        return List.of(this.detail);
    }

}
