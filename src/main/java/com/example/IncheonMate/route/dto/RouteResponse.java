package com.example.IncheonMate.route.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.place.dto.KakaoApiResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public class RouteResponse {

    //최근 길찾기 경로
    @Schema(description = "최근 길찾기 경로 기록 DTO")
    public record RecentRouteDto(
            @Schema(description = "최근 경로 ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            String recentSearchedRouteId,

            @Schema(description = "출발지 명칭", example = "인천국제공항")
            String departureName,

            @Schema(description = "출발지 경도(X)", example = "126.4406957")
            double departureLongitude, //경도

            @Schema(description = "출발지 위도(Y)", example = "37.4493277")
            double departureLatitude,//위도

            @Schema(description = "도착지 명칭", example = "송도센트럴파크")
            String arrivalName,

            @Schema(description = "도착지 경도(X)", example = "126.632865")
            double arrivalLongitude, //경도

            @Schema(description = "도착지 위도(Y)", example = "37.392784")
            double arrivalLatitude, //위도

            @Schema(description = "검색 일시", example = "2026-01-16T15:00:00")
            LocalDateTime searchedAt
    ){
        public static RecentRouteDto from(Member.RecentRoute recentRoute){
            return new RecentRouteDto(
                    recentRoute.getId(),
                    recentRoute.getDepartureName(),
                    recentRoute.getDepartureLocation().getX(),
                    recentRoute.getDepartureLocation().getY(),
                    recentRoute.getArrivalName(),
                    recentRoute.getArrivalLocation().getX(),
                    recentRoute.getArrivalLocation().getY(),
                    recentRoute.getSearchedAt()
            );
        }
    }

    //최근 검색 기록
    @Schema(description = "최근 검색어 기록 DTO")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecentSearchDto(
            @Schema(description = "최근 검색 ID (UUID)", example = "a1b2c3d4-...")
            String recentSearchedId,

            @Schema(description = "검색 키워드", example = "맛집")
            String keyword,

            @Schema(description = "검색 일시", example = "2026-01-16T15:05:00")
            LocalDateTime searchedAt//검색한 시간
    ){
        public static RecentSearchDto from(Member.RecentSearch recentSearch){
            return new RecentSearchDto(
                    recentSearch.getId(),
                    recentSearch.getKeyword(),
                    recentSearch.getSearchedAt()
            );
        }
    }

    //현재 검색한 장소(카카오)
    @Schema(description = "현재 검색된 장소 정보 (카카오맵 기반)")
    public record CurrentPlaceDto(
            @Schema(description = "카카오 장소 ID", example = "18577297")
            String kakaoPlaceId,

            @Schema(description = "장소명/업체명", example = "스타벅스 인천공항점")
            String placeName,

            @Schema(description = "카테고리 이름", example = "음식점 > 카페 > 커피전문점 > 스타벅스")
            String categoryName,

            @Schema(description = "전화번호", example = "1522-3232")
            String phone,

            @Schema(description = "지번 주소", example = "인천 중구 운서동 2851")
            String addressName,

            @Schema(description = "도로명 주소", example = "인천 중구 공항로 271")
            String roadAddressName,

            @Schema(description = "시스템 내 장소 등록 여부", example = "true")
            boolean isRegistered,

            @Schema(description = "경도(X)", example = "126.4460")
            double longitude,

            @Schema(description = "위도(Y)", example = "37.4485")
            double latitude
    ){
        public static CurrentPlaceDto from(KakaoApiResponseDto.DocumentDto document,boolean isRegistered){
            return new CurrentPlaceDto(
                    document.getId(),
                    document.getPlaceName(),
                    document.getCategoryName(),
                    document.getPhone(),
                    document.getAddressName(),
                    document.getRoadAddressName(),
                    isRegistered,
                    Double.parseDouble(document.getX()),
                    Double.parseDouble(document.getY())
            );

        }
    }

    //현재 길찾기 정보
    @Schema(description = "현재 길찾기 상세 경로 DTO (ODsay 응답 매핑)")
    public record CurrentRouteDto(
            // 필드가 추가되면 여기에 @Schema 추가
    ){
        public static CurrentRouteDto from(OdsayRouteSearchResponse response){
            return new CurrentRouteDto();
        }
    }
}