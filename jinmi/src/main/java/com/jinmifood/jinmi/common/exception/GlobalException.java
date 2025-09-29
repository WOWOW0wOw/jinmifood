package com.jinmifood.jinmi.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {

        log.error("CUstomException 핸들링", ex.getError().getDetail());

        ErrorResponse response = ErrorResponse.toErrorResponse(ex.getError());

        return ResponseEntity
                .status(ex.getError().getHttpStatus())
                .body(response);

    }
    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        String detailMessage = "아이디 또는 비밀번호가 일치하지 않습니다.";

        ErrorResponse response = new ErrorResponse(
                status,
                status.getReasonPhrase(),
                detailMessage);

        log.error("BadCredentialsException 핸들링", detailMessage);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    protected ResponseEntity<HttpStatus> catchException(RuntimeException ex) {
        log.error("처리되지 않은 예외 핸들링",ex);
        return ResponseEntity.internalServerError().body(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(MissingServletRequestPartException ex){
        String error = "누락된 필수 요청 값이 있습니다.";
        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST,ex.getMessage(),error);
        return ResponseEntity.badRequest().body(response);
    }


}
