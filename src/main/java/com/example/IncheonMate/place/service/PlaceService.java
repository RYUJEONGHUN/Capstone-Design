package com.example.IncheonMate.place.service;


import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.place.client.KakaoFeignClient;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.domain.type.PlaceCategory;
import com.example.IncheonMate.place.dto.KakaoApiResponseDto;
import com.example.IncheonMate.place.dto.PlaceData;
import com.example.IncheonMate.place.dto.PlaceRequestDto;
import com.example.IncheonMate.place.dto.PlaceResponseDto;
import com.example.IncheonMate.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import java.io.IOException;
import java.util.*;
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

    @Transactional
    public void registerPlace(PlaceRequestDto requestDto) {
        // 1. 이미 등록된 가게인지 확인 (중복 방지)
        if (placeRepository.findByKakaoId(requestDto.getKakaoId()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }

        // 2. 저장
        Place place = Place.builder()
                .kakaoId(requestDto.getKakaoId())
                .ourRating(requestDto.getOurRating())
                .tags(requestDto.getTags())
                .thumbnailUrl(requestDto.getThumbnailUrl())
                .build();

        Place savedPlace = placeRepository.save(place);
    }

    @Transactional // 엑셀 데이터 db 저장
    public String uploadPlaceExcel(MultipartFile file) {
        // 파일이 비어있는지
        if (file == null || file.isEmpty()) return "파일이 비어있습니다.";

        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // 1. 엑셀 데이터 파싱 & 중복 제거
            Map<String, PlaceData.RowData> rowDataMap = new LinkedHashMap<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String kakaoId = getCellString(row.getCell(0), formatter);
                if (kakaoId.isBlank()) continue;

                Double rating = parseDoubleOrNull(getCellString(row.getCell(1), formatter));
                List<String> tags = parseTags(getCellString(row.getCell(2), formatter));
                String comment = getCellString(row.getCell(3), formatter);
                String imageUrl = getCellString(row.getCell(4), formatter);

                rowDataMap.put(kakaoId, new PlaceData.RowData(kakaoId, rating, tags, comment, imageUrl));
            }

            if (rowDataMap.isEmpty()) return "등록할 데이터가 없습니다.";

            // 2. DB 조회 (Bulk Select)
            List<String> kakaoIds = new ArrayList<>(rowDataMap.keySet()); //N+1 문제 해결
            List<Place> existingPlaces = placeRepository.findAllByKakaoIdIn(kakaoIds);
            Map<String, Place> existingMap = existingPlaces.stream()
                    .collect(Collectors.toMap(Place::getKakaoId, p -> p));

            // 3. Insert or Update 준비
            List<Place> toSave = new ArrayList<>();

            for (String kakaoId : kakaoIds) {
                PlaceData.RowData rd = rowDataMap.get(kakaoId);
                Place place = existingMap.get(kakaoId);

                if (place == null) {
                    // 신규 생성 (New)
                    place = Place.builder()
                            .kakaoId(kakaoId)
                            .ourRating(rd.rating() != null ? rd.rating() : 0.0)
                            .tags(rd.tags())
                            .expertComment(rd.comment())
                            .thumbnailUrl(rd.imageUrl())
                            .build();
                } else {
                    // 업데이트 (Update)
                    place.updateMyData(rd.rating(), rd.tags(), rd.imageUrl(), rd.comment());
                }
                toSave.add(place);
            }

            // 4. 일괄 저장 // (Bulk Save)
            placeRepository.saveAll(toSave);

            return String.format("총 %d건 처리 완료 (신규: %d, 업데이트: %d)",
                    toSave.size(),
                    toSave.size() - existingPlaces.size(),
                    existingPlaces.size());

        } catch (IOException e) {
            return "엑셀 읽기 실패: " + e.getMessage();
        }
    }

    // --- Helper Methods ---

    private String getCellString(Cell cell, DataFormatter formatter) {
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private Double parseDoubleOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(x -> !x.isBlank())
                .map(tag -> tag.startsWith("#") ? tag : "#" + tag) // # 강제 부착
                .distinct()
                .toList();
    }

}