package com.jinmifood.jinmi.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GlobalException {

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
