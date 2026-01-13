package com.example.IncheonMate.place.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 모르는 필드 있어도 에러 안 나게 무시!
public class KakaoApiResponseDto {

    private MetaDto meta;                // 메타 정보
    private List<DocumentDto> documents; // 장소 목록

    // --- 1. Meta 정보 (페이지네이션 & 검색어 분석) ---
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaDto {
        @JsonProperty("total_count")
        private Integer totalCount;      // 검색된 전체 문서 수

        @JsonProperty("pageable_count")
        private Integer pageableCount;   // 노출 가능 문서 수

        @JsonProperty("is_end")
        private Boolean isEnd;           // 마지막 페이지 여부

        @JsonProperty("same_name")
        private SameNameDto sameName;    // 질의어 분석 정보 (키워드 검색 시에만 옴)
    }

    // --- 2. SameName 정보 (Meta 안에 들어감) ---
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SameNameDto {
        @JsonProperty("region")
        private List<String> region;     // 질의어에서 인식된 지역 리스트

        @JsonProperty("keyword")
        private String keyword;          // 지역 빼고 남은 키워드

        @JsonProperty("selected_region")
        private String selectedRegion;   // 선택된 지역
    }

    // --- 3. Document 정보 ---
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DocumentDto {
        @JsonProperty("id")
        private String id;               // 장소 ID

        @JsonProperty("place_name")
        private String placeName;        // 장소명

        @JsonProperty("category_name")
        private String categoryName;     // 카테고리 전체 (음식점 > 한식...)

        @JsonProperty("category_group_code")
        private String categoryGroupCode;// 카테고리 코드 (FD6, CE7)

        @JsonProperty("category_group_name")
        private String categoryGroupName;// 카테고리 이름 (음식점, 카페)

        @JsonProperty("phone")
        private String phone;            // 전화번호

        @JsonProperty("address_name")
        private String addressName;      // 지번 주소

        @JsonProperty("road_address_name")
        private String roadAddressName;  // 도로명 주소

        @JsonProperty("place_url")
        private String placeUrl;         // 상세 URL

        @JsonProperty("distance")
        private String distance;         // 거리 (중심좌표 기준, 없으면 빈값)

        @JsonProperty("x")
        private String x;                // 경도 (String)

        @JsonProperty("y")
        private String y;                // 위도 (String)
    }
}