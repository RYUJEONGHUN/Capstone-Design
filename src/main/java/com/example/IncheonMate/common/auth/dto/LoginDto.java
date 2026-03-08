package com.example.IncheonMate.common.auth.dto;

import com.example.IncheonMate.member.domain.type.PersonaType;

//로그인 관련 DTO
public class LoginDto {

    //게스트 로그인 요청 데이터
    public record GuestRequest(
            String nickname,
            PersonaType personaType
    ){}

    //게스트,유저 로그인 응답 데이터
    public record Response(
            String accessToken,
            String role
    ){
        public static Response from(Tokens tokens){
            return new Response(tokens.accessToken(),tokens.role());
        }
        public  static Response of(String accessToken,String role){
            return new Response(accessToken,role);
        }
    }
}
