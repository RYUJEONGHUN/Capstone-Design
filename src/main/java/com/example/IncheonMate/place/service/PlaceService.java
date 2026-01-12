package com.example.IncheonMate.place.service;


import com.example.IncheonMate.place.client.KakaoFeignClient;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.domain.type.PlaceCategory;
import com.example.IncheonMate.place.dto.KakaoApiResponseDto;
import com.example.IncheonMate.place.dto.PlaceResponseDto;
import com.example.IncheonMate.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    // FeignClient를 주입
    private final KakaoFeignClient kakaoFeignClient;

    private final PlaceRepository placeRepository;

    // API 키는 서비스에서 관리해서 헤더로 넘겨줍니다.
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> searchAndOverlay(String keyword) {

        // 1. 카카오 API 호출, 헤더만들기 (FeignClient 사용)
        String authHeader = "KakaoAK " + kakaoApiKey;

        KakaoApiResponseDto kakaoResult = kakaoFeignClient.searchByKeyword(authHeader, keyword);

        return mergeWithMyData(kakaoResult.getDocuments());
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> searchCategoryAndOverlay(PlaceCategory category, double x, double y) {

        String authHeader = "KakaoAK " + kakaoApiKey;

        // 카테고리 검색 호출
        KakaoApiResponseDto kakaoResult = kakaoFeignClient.searchByCategory(
                authHeader,
                category.getCode(),
                x, y,
                300, // 반경 300m
                "distance" // 거리순
        );

        return mergeWithMyData(kakaoResult.getDocuments());
    }

    private List<PlaceResponseDto> mergeWithMyData(List<KakaoApiResponseDto.DocumentDto> kakaoList) {
        if (kakaoList == null || kakaoList.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 카카오 ID들만 추출
        List<String> kakaoIds = kakaoList.stream()
                .map(KakaoApiResponseDto.DocumentDto::getId)
                .collect(Collectors.toList());

        // 2. 우리 DB에서 조회 (MongoDB IN 쿼리)
        List<Place> myPlaces = placeRepository.findAllByKakaoIdIn(kakaoIds);

        // 3. 빠른 조회를 위해 Map으로 변환 (Key: kakaoId, Value: Place 객체)
        Map<String, Place> myPlaceMap = myPlaces.stream()
                .collect(Collectors.toMap(Place::getKakaoId, Function.identity()));

        // 4. 카카오 데이터 + 내 데이터 합치기
        return kakaoList.stream()
                .map(k -> {
                    Place myData = myPlaceMap.get(k.getId());

                    // 4-1. 공통 정보 (무조건 카카오 데이터 기준)
                    PlaceResponseDto.PlaceResponseDtoBuilder builder = PlaceResponseDto.builder()
                            .kakaoId(k.getId())
                            .name(k.getPlaceName())
                            .category(k.getCategoryName())
                            .address(k.getRoadAddressName()) // 도로명 주소
                            .placeUrl(k.getPlaceUrl())
                            .x(parseCoordinate(k.getX())) // 아래 헬퍼 메서드 사용
                            .y(parseCoordinate(k.getY()));

                    // 4-2. 분기 처리 (우리 DB에 있냐 없냐)
                    if (myData != null) {
                        //  Case A: 우리 DB에 있는 '인증된 장소' -> 우리 데이터 덮어쓰기
                        return builder
                                .expertComment(myData.getExpertComment())
                                .isRegistered(true)
                                .ourRating(myData.getOurRating())     // 우리 별점
                                .thumbnailUrl(myData.getThumbnailUrl()) // 우리 사진
                                .tags(myData.getTags())               // 우리 태그
                                .build();
                    } else {
                        //  Case B: 우리 DB에 없는 '일반 장소' -> 기본값 채우기
                        return builder
                                .expertComment(null)
                                .isRegistered(false)
                                .ourRating(0.0)
                                .thumbnailUrl(null) // 프론트에서 기본 이미지 처리
                                .tags(Collections.emptyList())
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    // 카카오가 좌표를 String으로 주는데 가끔 빈 문자열일 때가 있어서 안전하게 변환해야 함
    private Double parseCoordinate(String coord) {
        try {
            if (coord == null || coord.isBlank()) {
                return 0.0;
            }
            return Double.parseDouble(coord);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}