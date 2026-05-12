package com.example.IncheonMate.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.lang.Nullable;

import java.util.List;

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
            @JsonProperty("provider") String fastApiChatProvider,
            @JsonProperty("places") List<PlaceDto> placesDto,
            @JsonProperty("route") List<RouteDto> routeDto

    ) {
    }

    public record PlaceDto(
            int rank,
            String placeName,
            String category,
            String subCategory,
            String address,
            String region,
            double rating,
            String kakaoId,
            @Nullable String imageUrl,
            double x,
            double y,
            @Nullable String naegiftId
    ){}

    public record RouteDto(
            int order,
            String placeName,
            String category,
            String subCategory,
            String address,
            String region,
            double rating,
            String kakaoId,
            @Nullable String imageUrl,
            double x,
            double y,
            @Nullable String naegiftId,
            double distanceFromPrev
    ) {}
}
