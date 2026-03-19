package com.example.IncheonMate.common.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GoogleOauthResponse{

    public record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn,
            @JsonProperty("token_type") String tokenType,
            String scope,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("id_token") String idToken
    ){}

    public record UserInfoResponse(
            String sub,
            String email,
            @JsonProperty("email_verified") Boolean emailVerified,
            String name,
            @JsonProperty("given_name") String givenName,
            @JsonProperty("family_name") String familyName,
            String picture,
            String locale
    ) {}
}
