package com.example.IncheonMate.place.controller;


import com.example.IncheonMate.place.domain.type.PlaceCategory;
import com.example.IncheonMate.place.dto.PlaceRequestDto;
import com.example.IncheonMate.place.dto.PlaceResponseDto;
import com.example.IncheonMate.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "장소(Place) API", description = "장소 키워드 검색, 카테고리 검색, 엑셀 업로드 기능") //
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    // 검색 API: GET /api/places/search/keyword?query=송도맛집
    @Operation(summary = "키워드 장소 검색", description = "카카오 API를 이용해 키워드로 장소를 검색하고, 우리 DB 데이터와 병합하여 반환.")
    @GetMapping("/search/keyword")
    public ResponseEntity<List<PlaceResponseDto>> searchPlaces(@RequestParam String query) {
        List<PlaceResponseDto> result = placeService.searchAndOverlay(query);
        return ResponseEntity.ok(result);
    }

    // 카테고리 API: GET /api/places/search/category?code?CT1&x=179.23&y=124.23
    @Operation(summary = "카테고리 기반 주변 검색", description = "현재 위치(x, y)를 기준으로 특정 카테고리의 장소를 검색.")
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

    @Operation(summary = "장소 개별 등록", description = "관리자가 장소를 하나씩 수동으로 등록.")
    @PostMapping
    public ResponseEntity<String> registerPlace(@RequestBody @Valid PlaceRequestDto requestDto) {
        placeService.registerPlace(requestDto);
        // 깔끔하게 문자열 메시지만 보냄
        return ResponseEntity.ok("장소 등록이 완료되었습니다!");
    }

    @Operation(summary = "장소 엑셀 대량 등록", description = "관리자용 엑셀 파일을 업로드하여 데이터를 일괄 등록/수정.")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        String result = placeService.uploadPlaceExcel(file);
        return ResponseEntity.ok(result);
    }
}
