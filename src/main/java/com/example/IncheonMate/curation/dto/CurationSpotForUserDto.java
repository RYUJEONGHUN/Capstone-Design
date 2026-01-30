package com.example.IncheonMate.curation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurationSpotForUserDto {
    private String placeId;
    private String placeName;
    private String kakaoId;
    private Double lat;
    private Double lng;
    private int triggerRadius;
    private String aiComment; // 유저 persona에 맞춘 1개
}