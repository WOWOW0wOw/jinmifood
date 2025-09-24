package com.jinmifood.jinmi.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@AllArgsConstructor
public enum ErrorException {

    DUPLICATE_ID(HttpStatus.CONFLICT, "이미 존재하는 아이디입니다");

    private final HttpStatus httpStatus;
    private final String detail;

    public List<String> getDetail() {
        return List.of(this.detail);
    }

}
