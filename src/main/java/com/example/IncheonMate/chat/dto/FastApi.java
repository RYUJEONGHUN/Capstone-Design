package com.example.IncheonMate.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FastApi {
    public record ChatRequestDto(
            @JsonProperty("user_input") String message,
            @JsonProperty("session_id") String identifier,
            @JsonProperty("persona_type") String persona
    ){
        public static FastApi.ChatRequestDto of(String message,String identifier,String persona){
            return new FastApi.ChatRequestDto(message,identifier,persona);
        }
    }

    public record ChatResponseDto(
            @JsonProperty("answer") String answer,
            @JsonProperty("error") String error
    ){}
}
