package com.example.IncheonMate.common.auth.dto;

public record Tokens(
        String accessToken, String refreshToken, String role
) {
}
