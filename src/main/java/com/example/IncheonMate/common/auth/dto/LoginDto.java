package com.example.IncheonMate.common.auth.dto;

import com.example.IncheonMate.member.domain.type.PersonaType;

import java.util.Map;

//로그인 관련 DTO
public class LoginDto {

    //게스트 로그인 요청 데이터
    public record GuestRequest(
            String nickname,
            String lang,
            PersonaType personaType
    ){}

    //유저 로그인을 위한 요청 데이터
    public record UserRequest(
            String code,
            String provider
    ){}

    public record Response(
            String accessToken,
            String role,
            // 아래 필드들은 ROLE_USER일 때는 null, ROLE_PENDING일 때만 값이 채워짐
            String nickname,
            String persona,
            String lang
    ) {
        // 1. 기존 유저 로그인용 (ROLE_USER)
        public static Response success(Tokens tokens) {
            return new Response(tokens.accessToken(), tokens.role(), null, null, null);
        }

        // 2. 신규 가입/게스트 이관용 (ROLE_PENDING)
        public static Response pending(String accessToken, String role, Map<String, String> profile) {
            if (profile == null) {
                return new Response(accessToken, role, null, null, null);
            }
            return new Response(
                    accessToken,
                    role,
                    profile.get("nickname"),
                    profile.get("persona"),
                    profile.get("lang")
            );
        }
    }
}
