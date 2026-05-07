package com.example.IncheonMate.common.auth.dto;

public record Tokens(
        String accessToken, String refreshToken, String role,String email
) {
    public static Tokens of(String accessToken,String refreshToken,String role,String email){
        return new Tokens(accessToken,refreshToken,role,email);
    }
}
