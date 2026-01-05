package com.example.IncheonMate.common.exception;

import lombok.Builder;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Builder
//모든 에러를 에러코드,에러메시지,에러발생시간으로 일관성 있게 응답하기 위해 만든 DTO
public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
) {
    //기본 버전(에러 코드에 정의된 메시지 사용)
    public static ResponseEntity<ErrorResponse> from(ErrorCode errorCode){
        return from(errorCode, errorCode.getMessage());
    }

    //동적 버전(상황에 따라 상세 메시지 직접 주입)
    public static ResponseEntity<ErrorResponse> from(ErrorCode errorCode,String message){
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
