package com.example.IncheonMate.place.service;


import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.place.client.KakaoFeignClient;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.domain.type.PlaceCategory;
import com.example.IncheonMate.place.dto.*;
import com.example.IncheonMate.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
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
@Slf4j
public class PlaceService {

    // FeignClient를 주입
    private final KakaoFeignClient kakaoFeignClient;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    // API 키는 서비스에서 관리해서 헤더로 넘겨줍니다.
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> searchAndOverlay(String keyword, String identifier, boolean isGuest) {

        // 1. 카카오 API 호출, 헤더만들기 (FeignClient 사용)
        String authHeader = "KakaoAK " + kakaoApiKey;

        KakaoApiResponseDto kakaoResult = kakaoFeignClient.searchByKeyword(authHeader, keyword);
        if (kakaoResult == null) {
            log.warn("[Place] 카카오맵 API 요청 실패 (Keyword: {})", keyword);
            throw new CustomException(ErrorCode.KAKAO_SERVER_ERROR);
        }
        log.debug("[Place] 카카오맵 API 요청 성공 (Keyword: {})", keyword);
        return mergeWithMyData(kakaoResult.getDocuments(), identifier, isGuest);
    }

    @Transactional(readOnly = true)
    public List<PlaceResponseDto> searchCategoryAndOverlay(PlaceCategory category, double x, double y, String identifier, boolean isGuest) {

        String authHeader = "KakaoAK " + kakaoApiKey;


        // 카테고리 검색 호출
        KakaoApiResponseDto kakaoResult = kakaoFeignClient.searchByCategory(
                authHeader,
                category.getCode(),
                x, y,
                1000, // 반경 300m
                "distance" // 거리순
        );
        if (kakaoResult == null) {
            log.warn("[Place] 카카오맵 API 요청 실패 (Category: {})", category.getCode());
            throw new CustomException(ErrorCode.KAKAO_SERVER_ERROR);
        }
        log.debug("[Place] 카카오맵 API 요청 성공 (Category: {})", category.getCode());

        if ("AT4".equals(category.getCode())) {
            KakaoApiResponseDto kakaoCT1Result = kakaoFeignClient.searchByCategory(
                    authHeader,
                    "CT1",
                    x, y,
                    1000,
                    "distance"
            );

            if (kakaoCT1Result != null && kakaoResult != null) {
                List<KakaoApiResponseDto.DocumentDto> kakaoList = kakaoResult.getDocuments();
                kakaoList.addAll(kakaoCT1Result.getDocuments());

                return mergeWithMyData(kakaoList, identifier, isGuest);
            }
        }


        return mergeWithMyData(kakaoResult.getDocuments(), identifier, isGuest);
    }

    private List<PlaceResponseDto> mergeWithMyData(List<KakaoApiResponseDto.DocumentDto> kakaoList, String identifier, boolean isGuest) {
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
        log.debug("[Place] DB의 Place 데이터 조회 완료");
        // 스트림 시작전 로그인한 유저 정보를 가져와서 찜한 카카오 ID만 Set으로 추출
        // 아래 stream안에서 조회하면 N+1(성능) 문제
        Set<String> bookmarkedKakaoIds;

        if (!isGuest) {
            List<Member.FavoritePlace> favoritePlaces = memberRepository.findByEmailOrElseThrow(identifier).getFavoritePlaces();
            bookmarkedKakaoIds = favoritePlaces.stream()
                    .map(Member.FavoritePlace::getKakaoPlaceId)
                    .collect(Collectors.toSet());
        } else {
            bookmarkedKakaoIds = Collections.emptySet();
        }
        log.debug("[Place] 찜목록 조회 완료");

        // 4. 카카오 데이터 + 내 데이터 합치기
        List<PlaceResponseDto> result = kakaoList.stream()
                .map(k -> {
                    Place myData = myPlaceMap.get(k.getId());
                    //추가. 찜 했는지 안했는지
                    boolean isBookmarked = bookmarkedKakaoIds.contains(k.getId());

                    //도로명 주소가 없으면 구 주소를 응답
                    String address = k.getRoadAddressName();
                    if (!StringUtils.hasText(address)) address = k.getAddressName();

                    // 4-1. 공통 정보 (무조건 카카오 데이터 기준)
                    PlaceResponseDto.PlaceResponseDtoBuilder builder = PlaceResponseDto.builder()
                            .kakaoId(k.getId())
                            .name(k.getPlaceName())
                            .category(k.getCategoryName())
                            .address(address) // 도로명 주소 or 지번 주소
                            .placeUrl(k.getPlaceUrl())
                            .x(parseCoordinate(k.getX())) // 아래 헬퍼 메서드 사용
                            .y(parseCoordinate(k.getY()))
                            .bookmarked(isBookmarked);

                    // 4-2. 분기 처리 (우리 DB에 있냐 없냐)
                    if (myData != null) {
                        //  Case A: 우리 DB에 있는 '인증된 장소' -> 우리 데이터 덮어쓰기
                        return builder
                                .expertComment(myData.getExpertComment())
                                .registered(true)
                                .ourRating(myData.getOurRating())     // 우리 별점
                                .thumbnailUrl(myData.getThumbnailUrl()) // 우리 사진
                                .tags(myData.getTags())               // 우리 태그
                                .naegiftUrl("https://shopuser-qa.naegift.com/" + myData.getNaegiftId() + "?channel_no=1")
                                .build();
                    } else {
                        //  Case B: 우리 DB에 없는 '일반 장소' -> 기본값 채우기
                        return builder
                                .expertComment(null)
                                .registered(false)
                                .ourRating(0.0)
                                .thumbnailUrl(null) // 프론트에서 기본 이미지 처리
                                .tags(Collections.emptyList())
                                .naegiftUrl(null)
                                .build();
                    }
                })
                .collect(Collectors.toList());

        log.info("[Place] 카테고리 검색 기본 데이터와 DB 데이터 병합 완료");
        return result;
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
            log.warn("[Place] 이미 등록된 장소 (관리자용)(KakaoPlaceId: {})", requestDto.getKakaoId());
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
        log.info("[Place] 장소 등록 완료 (관리자용)(PlaceId: {})", place.getId());
    }

    @Transactional // 엑셀 데이터 db 저장
    public String uploadPlaceExcel(MultipartFile file) {
        // 파일이 비어있는지
        if (file == null || file.isEmpty()) {
            log.warn("[Place] 파일 없음 (관리자용)");
            return "파일이 비어있습니다.";
        }

        DataFormatter formatter = new DataFormatter();

        //모든 탭(음식점,카페,관광지,호텔)의 데이터를 누적할 Map
        Map<String, PlaceData.RowData> rowDataMap = new LinkedHashMap<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            //엑셀 파일의 모든 시트를 순회
            for (Sheet sheet : workbook) {
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) continue;


                //시트별로 헤더 인덱스 매핑
                Map<String, Integer> headerMap = new HashMap<>();
                for (int col = 0; col < headerRow.getLastCellNum(); col++) {
                    String colName = getCellString(headerRow.getCell(col), formatter);
                    if (colName != null && !colName.isBlank()) {
                        headerMap.put(colName.trim(), col);
                    }
                }

                //해당 시트 데이터 파싱
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String kakaoId = getCellValue(row, "KakaoId", headerMap, formatter);
                    if (kakaoId == null || kakaoId.isBlank()) continue;

                    String name = getCellValue(row, "PlaceName", headerMap, formatter);
                    String address = getCellValue(row, "Address", headerMap, formatter);
                    PlaceCategory placeCategory = PlaceCategory.valueOf(getCellValue(row, "PlaceCategory", headerMap, formatter));
                    Double x = parseDoubleOrNull(getCellValue(row, "X", headerMap, formatter));
                    Double y = parseDoubleOrNull(getCellValue(row, "Y", headerMap, formatter));
                    String expertComment = getCellValue(row, "Comment", headerMap, formatter);
                    if (expertComment != null) expertComment = expertComment.replaceAll("[\r\n]{2,}", "\n");
                    Double ourRating = parseDoubleOrNull(getCellValue(row, "Rating", headerMap, formatter));
                    String thumbnailUrl = getCellValue(row, "Image", headerMap, formatter);
                    String naegiftId = getCellValue(row, "naegiftId", headerMap, formatter);
                    List<String> tags = parseTags(getCellValue(row, "Tags", headerMap, formatter));

                    rowDataMap.put(kakaoId, new PlaceData.RowData(kakaoId, name, address, placeCategory, x, y, expertComment, ourRating, thumbnailUrl, naegiftId, tags));
                }
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
                            .name(rd.name())
                            .address(rd.address())
                            .placeCategory(rd.placeCategory())
                            .x(rd.x())
                            .y(rd.y())
                            .expertComment(rd.expertComment())
                            .ourRating(rd.ourRating())
                            .thumbnailUrl(rd.thumbnailUrl())
                            .naegiftId(rd.naegiftId())
                            .tags(rd.tags())
                            .build();
                } else {
                    // 업데이트 (Update)
                    place.updateMyData(rd.ourRating(), rd.tags(), rd.thumbnailUrl(), rd.expertComment());
                }
                toSave.add(place);
            }

            // 4. 일괄 저장 // (Bulk Save)
            placeRepository.saveAll(toSave);
            log.info("[Place] 엑셀 데이터 저장 완료 (관리자용)(신규: {}, 업데이트: {})",toSave.size(), toSave.size() - existingPlaces.size());

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
        if (cell == null || cell.getCellType() == CellType.BLANK) return "";
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

    private String getCellValue(Row row, String colName, Map<String, Integer> headerMap, DataFormatter formatter) {
        Integer colIndex = headerMap.get(colName);
        if (colIndex == null) return null;
        return getCellString(row.getCell(colIndex), formatter);
    }

    public List<Place> searchByIntent(PlaceSearchRequest request) {

        // 1. AI 키워드("CAFE")를 DB 코드("CE7")로 변환
        String categoryCode = PlaceCategory.fromAIKeyword(request.getCategory()).getCode();

        // 1. Repository의 @Query 호출
        List<Place> results = placeRepository.findByAiIntent(
                request.getLocation(), categoryCode, request.getVibe(), request.getCompanion()
        );

        // 2. 만약 검색 결과가 없다면? (사용자 경험을 위한 예외 처리)
        if (results.isEmpty()) {
            // 위치랑 카테고리만으로 더 넓게 재검색하는 로직을 넣을 수 있어.
            log.warn("[Place] 해당하는 조건의 장소 없음 (Location: {}, Category: {})", request.getLocation(), request.getCategory());
            return placeRepository.findByAddressContainingAndCategoryGroup(
                    request.getLocation(),
                    categoryCode
            );
        }

        log.info("[Place] FastAPI 위치,카테고리 기반 장소 검색 성공");
        // 3. 별점(ourRating) 높은 순으로 정렬해서 반환
        return results.stream()
                .sorted(Comparator.comparing(Place::getOurRating).reversed())
                .toList();
    }

}