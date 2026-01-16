package com.example.IncheonMate.route.controller;

import com.example.IncheonMate.common.exception.ErrorResponse;
import com.example.IncheonMate.route.dto.OdsayRouteSearchResponse;
import com.example.IncheonMate.route.dto.RouteRequest;
import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.route.dto.RouteResponse;
import com.example.IncheonMate.route.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/* 길찾기 탭 필요한 기능 목록
1.길찾기 초기화면: 최근 길찾기 기록 보여주는 기능-get/getRecentRoutes            (/api/route/history/paths)
2.검색 화면: 목적지 검색화면으로 넘어갈때 최근 검색 기록을 보여주는 기능-get/getRecentPlaces            (/api/route/history/places)
3.검색 화면: 검색어를 입력하면 검색어에 맞는 장소를 보여주면서 키워드를 저장하는 기능-get+URI Param/searchAndSavePlaces         (/api/route/places)
3.1 결과 보여주는 방법: 입력을 멈춘 사이에 API요청을 해서 보여주는 방식
3.2 보여줄 내용: 카카오 API에 등록한 장소만 보여줌
3.3 검색 히스토리 저장 내용: 검색한 키워드만 저장
4.길찾기 조회 완료 화면: 출발지와 목적지를 입력하고 '길찾기'를 누르면 그에 맞는 경로들을 보여주면서 저장하는 기능-POST/findAndSaveRoutes   (/api/route/paths)

/api/route/history/**:기록과 관련된 모든 것
/api/route/places:장소 데이터
/api/route/paths:실제 이동 경로(ODsay)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/route")
@Tag(name = "Route Management API", description = "길찾기 기능")
public class RouteController {

    private final RouteService routeService;

    //1.길찾기 초기화면: 최근 길찾기 기록 보여주는 기능-get/getRecentRoutes(/api/route/history/paths)
    @Operation(summary = "최근 길찾기 기록", description = "최근 길찾기 내역을 최대 20개까지 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 길찾기 기록 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RouteResponse.RecentRouteDto.class)))),
            @ApiResponse(responseCode = "404", description = "멤버나 길찾기 기록을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/history/paths")
    public ResponseEntity<List<RouteResponse.RecentRouteDto>> getRecentRoutes(@AuthenticationPrincipal CustomOAuth2User user) {
        String email = user.getEmail();
        log.info("최근 길찾기 내역 조회 요청: {}",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(routeService.getRecentRoutes(email));
    }

    //2.검색 화면: 목적지 검색화면으로 넘어갈때 최근 검색 기록을 보여주는 기능-get/getrecentSearches(/api/route/history/searches)
    @Operation(summary = "최근 키워드 검색 기록", description = "최근 키워드 내역을 최대 20개까지 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 키워드 기록 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RouteResponse.RecentSearchDto.class)))),
            @ApiResponse(responseCode = "404", description = "멤버나 키워드 기록을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/history/places")
    public ResponseEntity<List<RouteResponse.RecentSearchDto>> getRecentPlaces(@AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("최근 검색 내역 조회 요청: {}",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(routeService.getRecentPlaces(email));
    }

    //3.검색 화면: 검색어를 입력하면 검색어에 맞는 장소를 보여주면서 키워드나 장소를 저장하는 기능-get+URI Param/searchAndSavePlaces(/api/route/searchs)
    //입력을 멈춘사이에 장소 보여준다고 가정하고 작성함
    @Operation(summary = "키워드로 장소 검색 요청", description = "카카오 맵 API에서 키워드로 장소 검색을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카카오 키워드 장소 검색 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RouteResponse.CurrentPlaceDto.class)))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "카카오 맵 API 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/places")
    public ResponseEntity<List<RouteResponse.CurrentPlaceDto>> searchAndSavePlaces(@AuthenticationPrincipal CustomOAuth2User user,
                                                                @RequestParam(value = "save", defaultValue = "false") boolean save,
                                                                @RequestParam("keyword") String keyword){
        String email = user.getEmail();
        //0.3초나 0.5초간 입력을 멈출 때마다 로깅을 하면 로그가 너무 많아져 줄이기 위해서 DEBUG레벨로 로깅
        log.debug("실시간 장소 검색 - 사용자: {}, 키워드: '{}'", email, keyword);

        return ResponseEntity.status(HttpStatus.OK)
                .body(routeService.searchAndSavePlaces(email, keyword,save));
    }

    //4.길찾기 조회 완료 화면: 출발지와 목적지를 입력하고 '길찾기'를 누르면 그에 맞는 경로들을 보여주면서 저장하는 기능-POST/findAndSavePaths(/api/route/paths)
    //최근 경로 검색 기록을 저장하기 위해서 출발지,목적지 이름도 받아야한다. -> POST로 변경
    //*********************ODsay에서 '도시내 길찾기'와 '도시간 길찾기' 출력 데이터가 다르고 여기에서는 도시내 길찾기 응답만 받기 때문에 프론트에서 "도시간 길찾기"는 안된다고 명시해야함*********
    @Operation(summary = "길찾기 요청 및 저장", description = "ODsay 대중교통 길찾기 API로 경로를 검색하고, 검색 기록을 저장합니다.")
    @ApiResponses(value = {
            // 200 OK: 성공 (List가 아닌 단일 DTO 반환이므로 ArraySchema 제거)
            @ApiResponse(responseCode = "200", description = "경로 검색 성공",
                    content = @Content(schema = @Schema(implementation = RouteResponse.CurrentRouteDto.class))),
            // 400 Bad Request: ODsay 에러 코드 매핑 (-8, -9, 6, -98)
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (거리 700m 이내, 서비스 지역 아님, 입력값 오류)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            // 404 Not Found: 사용자 없음, 결과 없음, 정류장 없음 (3, 4, 5, -99)
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음 (사용자 없음, 주변 정류장 없음, 검색 결과 없음)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            // 500 Internal Server Error: ODsay 서버 에러 및 내부 로직 에러
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 (ODsay 연동 실패 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/paths")
    public ResponseEntity<RouteResponse.CurrentRouteDto> findAndSaveRoutes(@AuthenticationPrincipal CustomOAuth2User user,
                                                                                 @RequestBody @Valid RouteRequest.RouteSearchRequest routeSearchRequest){
        String email = user.getEmail();
        log.info("길찾기 요청 - 사용자: {}, 출발지: {}, 목적지: {}",email,routeSearchRequest.departureName(),routeSearchRequest.arrivalName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(routeService.findAndSaveRoutes(email,routeSearchRequest));
    }

    //길찾기 조회 데이터 선택을 위한 임시 컨트롤러
    @GetMapping("/paths/all")
    public ResponseEntity<OdsayRouteSearchResponse> findRoutes(@RequestParam("sx") String sx,@RequestParam("sy")String sy,@RequestParam("ex")String ex,@RequestParam("ey")String ey){
        log.info("전체 Odsay 응답 JSON 조회 요청");
        return ResponseEntity.ok(routeService.getWholeOdsay(sx,sy,ex,ey));
    }
}
