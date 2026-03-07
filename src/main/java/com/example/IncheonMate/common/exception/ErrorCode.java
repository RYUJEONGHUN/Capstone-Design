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
    NOT_A_SERVICE_AREA(HttpStatus.BAD_REQUEST,"ROUTE 005","서비스 지역이 아닙니다."),
    DISTANCE_TOO_SHORT(HttpStatus.BAD_REQUEST, "ROUTE 006", "출, 도착지가 700m 이내입니다."),
    MISSING_REQUIRED_INFO(HttpStatus.BAD_REQUEST,"AUTH 001", "이메일과 이름 제공에 동의해야 서비스를 이용할 수 있습니다."),


    //404
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER 001","사용자를 찾을 수 없습니다."),
    PERSONA_NOT_FOUND(HttpStatus.NOT_FOUND, "PERSONA 001","선택 가능한 페르소나가 없습니다."),
    CHAT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT 001","채팅 세션을 찾을 수 없습니다."),
    ROUTE_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUTE 001", "길찾기 기록을 찾을 수 없습니다."),
    SEARCH_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND,"ROUTE 002","검색 기록을 찾을 수 없습니다."),
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND,"ROUTE 004","출발지 또는 도착지 주변에 정류장이 없습니다."),
    NO_SEARCH_RESULT(HttpStatus.NOT_FOUND, "ROUTE 007", "대중교통 길찾기 검색 결과가 없습니다."),

    //409
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON 409", "이미 존재하는 리소스입니다."),

    //500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"SERVER 001", "서버 내부 오류가 발생했습니다."),
    ODSAY_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"ROUTE 003","ODsay 서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
