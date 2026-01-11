package com.example.IncheonMate.place.controller;


import com.example.IncheonMate.place.domain.type.PlaceCategory;
import com.example.IncheonMate.place.dto.PlaceResponseDto;
import com.example.IncheonMate.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
