package com.example.IncheonMate.reward.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Naegift {

    public record RequestDto(
            @JsonProperty("recipientEmail") String email,
            @JsonProperty("uuid") String couponId
    ){}

    public record SuccessResponseDto(
        int resultCode,
        String resultMessage,
        Map<String, Object> data
    ){}

    public record ErrorResponseDto(
            int statusCode,
            String timestamp,
            String path,
            String message
    ){}
}
