package com.example.IncheonMate.place.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceResponseDto {

    // --- 1. 카카오에서 가져온 기본 정보 ---
    private String kakaoId;      // 식별자
    private String name;         // 가게 이름
    private String category;     // 카테고리 상세
    private String address;      // 주소
    private Double x;            // 경도 (Double 변환됨)
    private Double y;            // 위도 (Double 변환됨)
    private String placeUrl;     // 카카오맵 링크

    // --- 2. 인천메이트 고유 정보 (DB에서 덮어쓸 내용) ---
    private boolean isRegistered; // 우리 서비스 인증 장소인지?
    private Double ourRating;     // 우리만의 별점 (없으면 0.0)
    private String thumbnailUrl;  // 대표 사진 URL
    private List<String> tags;    // 태그 목록
}