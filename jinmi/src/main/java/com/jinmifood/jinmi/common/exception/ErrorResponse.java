package com.jinmifood.jinmi.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private final Integer status;
    private final String message;
    private final List<String> errors;

    public ErrorResponse(HttpStatus status, String message, String error) {
        this.status = status.value();
        this.message = message;
        this.errors = List.of(error);
    }

    public ErrorResponse(HttpStatus status, String message, List<String> errors) {
        this.status = status.value();
        this.message = message;
        this.errors = errors;
    }

    public static ErrorResponse toErrorResponse(ErrorException errorCode) {
        return new ErrorResponse(
                errorCode.getHttpStatus(),
                errorCode.name(),
                errorCode.getDetail());
    }
}
