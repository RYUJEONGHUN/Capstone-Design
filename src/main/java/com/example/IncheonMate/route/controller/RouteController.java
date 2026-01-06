package com.example.IncheonMate.route.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.route.dto.RouteResponse;
import com.example.IncheonMate.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/* 길찾기 탭 필요한 기능 목록
1.길찾기 초기화면: 최근 길찾기 기록 보여주는 기능-get/getRecentRoutes            (/api/route/history/paths)
2.검색 화면: 목적지 검색화면으로 넘어갈때 최근 검색 기록을 보여주는 기능-get/getRecentPlaces            (/api/route/history/places)
3.검색 화면: 검색어를 입력하면 검색어에 맞는 장소를 보여주면서 키워드를 저장하는 기능-get+URI Param/searchPlaces         (/api/route/places)
3.1 결과 보여주는 방법: 입력을 멈춘 사이에 API요청을 해서 보여주는 방식
3.2 보여줄 내용: 카카오 API에 등록한 장소만 보여줌
3.3 검색 히스토리 저장 내용: 검색한 키워드만 저장
4.길찾기 조회 완료 화면: 출발지와 목적지를 입력하고 '길찾기'를 누르면 그에 맞는 경로들을 보여주면서 저장하는 기능-get+URI Param/findRoutes   (/api/route/paths)

/api/route/history/**:기록과 관련된 모든 것
/api/route/places:장소 데이터
/api/route/paths:실제 이동 경로(ODsay)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/route")
public class RouteController {

    private final RouteService routeService;

    //1.길찾기 초기화면: 최근 길찾기 기록 보여주는 기능-get/getRecentRoutes(/api/route/history/paths)
    @GetMapping("/history/paths")
    public ResponseEntity<List<RouteResponse.RecentRouteDto>> getRecentRoutes(@AuthenticationPrincipal CustomOAuth2User user) {

        return null;
    }

    //2.검색 화면: 목적지 검색화면으로 넘어갈때 최근 검색 기록을 보여주는 기능-get/getrecentSearches(/api/route/history/searches)
    @GetMapping("/history/places")
    public ResponseEntity<List<RouteResponse.RecentPlaceDto>> getRecentPlaces(@AuthenticationPrincipal CustomOAuth2User user){

        return null;
    }

    //3.검색 화면: 검색어를 입력하면 검색어에 맞는 장소를 보여주면서 키워드나 장소를 저장하는 기능-get+URI Param/searchPlaces(/api/route/searchs)
    //입력을 멈춘사이에 장소 보여준다고 가정하고 작성함
    @GetMapping("/places")
    public ResponseEntity<List<RouteResponse.CurrentPlaceDto>> searchPlaces(@AuthenticationPrincipal CustomOAuth2User user,
                                                                @RequestParam(value = "save", defaultValue = "false") boolean save,
                                                                @RequestParam("keyword") String keyword){
        return null;
    }

    //4.길찾기 조회 완료 화면: 출발지와 목적지를 입력하고 '길찾기'를 누르면 그에 맞는 경로들을 보여주면서 저장하는 기능-get+URI Param/findPaths(/api/route/paths)
    //*********************오디세이에서 도시내 길찾기 응답만 받기 떄문에 프론트에서 "도시간 길찾기"는 안된다고 명시해야함*********
    @GetMapping("/paths")
    public ResponseEntity<List<RouteResponse.CurrentRouteDto>> findRoutes(@AuthenticationPrincipal CustomOAuth2User user,
                                                                        @RequestParam("sx") String sx, @RequestParam("sy") String sy,
                                                                        @RequestParam("ex") String ex, @RequestParam("ey") String ey){
        return null;
    }
}
