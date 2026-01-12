package com.example.IncheonMate.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
//상황에 맞는 에러를 응답하기 위한 에러 모음
public enum ErrorCode {
    //400
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON 001", "잘못된 입력값입니다."),
    INVALID_KEYWORD_VALUE(HttpStatus.BAD_REQUEST, "CHAT 002","검색어가 없습니다."),
    INVALID_SASANG_TYPE(HttpStatus.BAD_REQUEST, "MEMBER 003", "체질 결과를 도출 할 수 없습니다."),

    //404
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER 001","사용자를 찾을 수 없습니다."),
    PERSONA_NOT_FOUND(HttpStatus.NOT_FOUND, "PERSONA 001","선택 가능한 페르소나가 없습니다."),
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT 001","채팅 세션을 찾을 수 없습니다."),

    //409
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON 409", "이미 존재하는 리소스입니다."),

    //500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"SERVER 001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
