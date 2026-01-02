package com.example.IncheonMate.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e){
        ErrorCode errorCode = e.getErrorCode();
        return ErrorResponse.from(errorCode,e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[Server Error] 예상하지 못한 예외 발생: ", e);
        return ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    // @Valid 검증 실패 시 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 1. 에러가 발생한 필드 중 첫 번째 필드를 가져옵니다.
        FieldError fieldError = e.getBindingResult().getFieldError();

        // 2. "필드명: 에러메시지" 형태로 메시지를 가공합니다. (예: "nickname: 닉네임은 2글자 이상이어야 합니다.")
        // fieldError가 null일 경우에 대한 방어 로직 포함
        String message = (fieldError != null)
                ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                : "잘못된 입력값입니다.";

        log.warn("Validation Failed: {}", message);

        // 3. 기존에 정의한 INVALID_INPUT_VALUE 에러 코드와 가공한 메시지를 반환합니다.
        return ErrorResponse.from(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    // JSON 파싱 에러 및 Enum 바인딩 실패 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = "잘못된 형식의 요청 데이터입니다.";

        // Enum 값 불일치 에러인 경우 메시지를 구체화함
        if (e.getCause() instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
            message = "유효하지 않은 Enum 값입니다. 정해진 값을 확인해주세요.";
        }

        log.warn("[JSON Parse Error] {}", e.getMessage());
        return ErrorResponse.from(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}
