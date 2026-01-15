package com.example.IncheonMate.route.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.place.dto.KakaoApiResponseDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.mongodb.core.aggregation.VectorSearchOperation;

import java.time.LocalDateTime;

public class RouteResponse {
    /*
    public static class RecentSearch{
        private String id; //수동 UUID
        private String keyword; //검색어
        private GeoJsonPoint location;//좌표
        @CreatedDate
        private LocalDateTime searchedAt; //검색한 시간
    }
    public static class RecentRoute{
        private String id; //수동 UUID
        private String departureName; //출발 장소 이름
        private String arrivalName; //도착 장소 이름
        private GeoJsonPoint departureLocation; //출발 장소 좌표
        private GeoJsonPoint arrivalLocation;//도착 장소 좌표
        @CreatedDate
        private LocalDateTime searchedAt; //검색한 시간
        //@Nullable private int searchPathType; //경로검색결과 정렬방식을 적용하려면 주석 해제(모두,지하철만,버스만)
    }
     */

    //최근 길찾기 경로
    public record RecentRouteDto(
            String recentSearchedRouteId,
            String departureName,
            double departureLongitude, //경도
            double departureLatitude,//위도
            String arrivalName,
            double arrivalLongitude, //경도
            double arrivalLatitude, //위도
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RecentSearchDto(
        String recentSearchedId,
        String keyword,
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
    public record CurrentPlaceDto(
        String kakaoPlaceId,
        String placeName, //장소명,업체명
        String categoryName, //카테고리 이름
        String phone,
        String addressName, //지번 주소
        String roadAddressName,//도로명 주소
        boolean isRegistered,
        double longitude,
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
    //OdsayRouteSearchResponse에서 필요한 key값들을 정리해서 알려주면 이 DTO로 선언
    //나중에
    public record CurrentRouteDto(

    ){
        public static CurrentRouteDto from(OdsayRouteSearchResponse response){
            return new CurrentRouteDto();
        }
    }
}
