package com.example.IncheonMate.common.auth.dto;

import com.example.IncheonMate.member.domain.type.PersonaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;

//로그인 관련 DTO
public class LoginDto {

    //게스트 로그인 요청 데이터
    public record GuestRequest(
            String lang,
            PersonaType personaType,
            boolean isPrivacyPolicyAgreed,//개인정보 처리방침 동의
            boolean isLocationServiceAgreed, //위치기반 서비스 동의
            boolean isTermsOfServiceAgreed //개인정보 동의
    ){}

    //유저 로그인을 위한 요청 데이터
    public record UserRequest(
            String code,
            String provider
    ){}

    //게스트 정보
    public record GuestProfile(
            String nickname,
            PersonaType persona,
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
            PersonaType persona,
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
