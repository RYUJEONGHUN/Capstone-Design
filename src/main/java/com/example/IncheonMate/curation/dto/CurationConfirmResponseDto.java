package com.example.IncheonMate.curation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CurationConfirmResponseDto {
    private String placeId;
    private String kakaoId;
    private String placeName;
    private Double lat;
    private Double lng;

    private String aiComment;       // persona에 맞춘 1개
    private Double ourRating;
    private String expertComment;
    private String thumbnailUrl;
    private List<String> tags;
}