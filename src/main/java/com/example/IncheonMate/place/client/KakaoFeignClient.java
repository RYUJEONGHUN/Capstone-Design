package com.example.IncheonMate.place.client;

import com.example.IncheonMate.place.dto.KakaoApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

// url: 요청을 보낼 기본 주소
@FeignClient(name = "kakao-client", url = "https://dapi.kakao.com")
public interface KakaoFeignClient {

    // 1. 키워드 검색
    @GetMapping("/v2/local/search/keyword.json")
    KakaoApiResponseDto searchByKeyword(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("query") String query
    );

    // 2. 카테고리 검색
    @GetMapping("/v2/local/search/category.json")
    KakaoApiResponseDto searchByCategory(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("category_group_code") String categoryCode,
            @RequestParam("x") double x,
            @RequestParam("y") double y,
            @RequestParam("radius") int radius,
            @RequestParam("sort") String sort
    );
}