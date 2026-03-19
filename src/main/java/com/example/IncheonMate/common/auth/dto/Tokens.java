package com.example.IncheonMate.common.auth.dto;

public record Tokens(
        String accessToken, String refreshToken, String role
) {
    public static Tokens of(String accessToken,String refreshToken,String role){
        return new Tokens(accessToken,refreshToken,role);
    }
}
