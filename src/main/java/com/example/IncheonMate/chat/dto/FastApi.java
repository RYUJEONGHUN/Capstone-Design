package com.example.IncheonMate.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastApi {
    public record ChatRequestDto(
            @JsonProperty("user_input") String message,
            @JsonProperty("session_id") String identifier,
            @JsonProperty("persona_type") String persona,
            @JsonProperty("mbti_type") String mbti,
            @JsonProperty("sasang_type") String sasang
    ) {
        public static FastApi.ChatRequestDto of(String message, String identifier, String persona, String mbti, String sasang) {
            return new FastApi.ChatRequestDto(message, identifier, persona, mbti, sasang);
        }
    }

    //에러 응답 추가해야함
    public record ChatResponseDto(
            @JsonProperty("answer") String answer,
            @JsonProperty("isCourse") boolean isCourse,
            @JsonProperty("responseType") String fastApiChatResponseType,
            @JsonProperty("provider") String fastApiChatProvider
    ) {
    }
}
