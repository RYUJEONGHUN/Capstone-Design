package com.example.IncheonMate.common.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KakaoOauthResponse {

    //카카오 API에 code를 보내서 accessToken,refreshToken을 받아올때 사용
    public record TokenResponse(
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("id_token") String idToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("refresh_token_expires_in") int refreshTokenExpiresIn,
            @JsonProperty("scope") String scope
    ) {
    }

    //카카오 API에 accessToken을 보내서 유저 이메일과 실명을 받아올때 사용
    public record UserInfoResponse(
            Long id, // 회원 번호 (필수)
            @JsonProperty("connected_at") String connectedAt, // 연결 시각
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount // 카카오 계정 정보
    ) {
        // 내부 Record 1: 카카오 계정 (이메일 포함)
        public record KakaoAccount(
                @JsonProperty("has_email") Boolean hasEmail,
                @JsonProperty("email_needs_agreement") Boolean emailNeedsAgreement,
                @JsonProperty("is_email_valid") Boolean isEmailValid,
                @JsonProperty("is_email_verified") Boolean isEmailVerified,
                @JsonProperty("email") String email, // ★ 이메일

                @JsonProperty("profile_nickname_needs_agreement") Boolean profileNicknameNeedsAgreement,
                @JsonProperty("profile") Profile profile // ★ 프로필 (닉네임 포함)
        ) {
        }

        // 내부 Record 2: 프로필 (닉네임 포함)
        public record Profile(
                @JsonProperty("nickname") String nickname, // ★ 닉네임
                @JsonProperty("thumbnail_image_url") String thumbnailImageUrl,
                @JsonProperty("profile_image_url") String profileImageUrl,
                @JsonProperty("is_default_image") Boolean isDefaultImage
        ) {
        }
    }
}
