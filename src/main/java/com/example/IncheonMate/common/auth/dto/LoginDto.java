package com.example.IncheonMate.common.auth.dto;

import com.example.IncheonMate.member.domain.type.PersonaType;

//로그인 관련 DTO
public class LoginDto {

    //게스트 로그인 요청 데이터
    public record GuestRequest(//--페르소나와 언어만 받는 걸로 수정해야함
            String lang,
            PersonaType personaType
    ){}

    //유저 로그인을 위한 요청 데이터
    public record UserRequest(
            String code,
            String provider
    ){}

    //게스트 정보
    public record GuestProfile(
            String nickname,
            String persona,
            String lang
    ){}

    // Service -> Controller 전달용 Wrapper 객체 추가
    public record GuestLoginResult(
            Tokens tokens,
            GuestProfile guestProfile
    ) {}

    public record Response(
            String accessToken,
            String role,
            String nickname,
            String persona,
            String lang
    ) {
        public static Response from(Tokens tokens, GuestProfile guestProfile) {
            return new Response(tokens.accessToken(), tokens.role(), guestProfile.nickname(), guestProfile.persona(), guestProfile.lang());
        }

        public static Response onlyToken(Tokens tokens){
            return new Response(tokens.accessToken(),tokens.role(),null,null,null);
        }
    }
}
