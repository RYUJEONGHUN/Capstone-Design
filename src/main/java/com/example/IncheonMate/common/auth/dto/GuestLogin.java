package com.example.IncheonMate.common.auth.dto;

import com.example.IncheonMate.member.domain.type.PersonaType;

import java.time.LocalDateTime;

//게스트 로그인 DTO
public class GuestLogin {

    //필요 데이터
    public record RequestDto(
            String nickname,
            PersonaType personaType
    ){}

    //전송 데이터
    public record ResponseDto(
            String accessToken,
            String role
    ){
        public static ResponseDto from(Tokens tokens){
            return new ResponseDto(tokens.accessToken(),tokens.role());
        }
    }
}
