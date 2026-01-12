package com.example.IncheonMate.place.controller;


import com.example.IncheonMate.place.domain.type.PlaceCategory;
import com.example.IncheonMate.place.dto.PlaceResponseDto;
import com.example.IncheonMate.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // 검색 API: GET /api/places/search/keyword?query=송도맛집
    @GetMapping("/search/keyword")
    public ResponseEntity<List<PlaceResponseDto>> searchPlaces(@RequestParam String query) {
        List<PlaceResponseDto> result = placeService.searchAndOverlay(query);
        return ResponseEntity.ok(result);
    }

    // 카테고리 API: GET /api/places/search/category?code?CT1&x=179.23&y=124.23
    @GetMapping("/search/category")
    public ResponseEntity<List<PlaceResponseDto>> searchCategoryPlaces(@RequestParam PlaceCategory category,@RequestParam double x, @RequestParam double y) {
        List<PlaceResponseDto> result = placeService.searchCategoryAndOverlay(category,x,y);
        return ResponseEntity.ok(result);
    }

    // Enum -> List<Map> 변환
    // 결과 예시: [{"code": "FD6", "name": "음식점"}, {"code": "CE7", "name": "카페"} ...]
    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getCategories() {
        List<Map<String, String>> result = Arrays.stream(PlaceCategory.values())
                .map(c -> Map.of(
                        "code", c.getCode(),
                        "name", c.getDescription()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
