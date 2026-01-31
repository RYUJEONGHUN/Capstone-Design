package com.example.IncheonMate.route.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.place.client.KakaoFeignClient;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.dto.KakaoApiResponseDto;
import com.example.IncheonMate.place.repository.PlaceRepository;
import com.example.IncheonMate.route.client.OdsayClient;
import com.example.IncheonMate.route.dto.OdsayRouteSearchResponse;
import com.example.IncheonMate.route.dto.RouteRequest;
import com.example.IncheonMate.route.dto.RouteResponse;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RouteService {

    private final OdsayClient odsayClient;
    private final MemberRepository memberRepository;
    private final MongoTemplate mongoTemplate;//기록 개수 제한을 위해서 MongoTempalte를 사용
    private final KakaoFeignClient kakaoFeignClient;
    private final PlaceRepository placeRepository;

    @Value("${ODSAY_KEY}")
    private String apiKey;

    @Value("${kakao.api.key}")
    private String kakaoKey;

    //1.길찾기 초기화면: 최근 길찾기 기록을 반환하는 메소드(/api/route/history/paths)
    public List<RouteResponse.RecentRouteDto> getRecentRoutes(String email) {
        List<RouteResponse.RecentRouteDto> result = memberRepository.findByEmailOrElseThrow(email)
                .getRecentRoutes().stream()
                .map(route -> {
                    try {
                        return RouteResponse.RecentRouteDto.from(route);//정상
                    } catch (CustomException e) {
                        log.error("길찾기 기록 조회중 오류 발생: {}", email); //비정상
                        throw new CustomException(ErrorCode.ROUTE_HISTORY_NOT_FOUND);
                    }
                })
                .toList();

        log.info("최근 경로 검색 결과 {}개 반환: {}", result.size(), email);
        return result;
    }

    //2.검색 화면: 목적지 검색화면으로 넘어갈때 최근 검색 기록을 반환하는 메소드(/api/route/history/searches)
    public List<RouteResponse.RecentSearchDto> getRecentPlaces(String email) {
        List<RouteResponse.RecentSearchDto> result = memberRepository.findByEmailOrElseThrow(email)
                .getRecentSearches().stream()
                .map(search -> {
                    try {
                        return RouteResponse.RecentSearchDto.from(search);//정상
                    } catch (CustomException e) {
                        log.error("검색 기록 조회중 오류 발생: {}", email); //비정상
                        throw new CustomException(ErrorCode.SEARCH_HISTORY_NOT_FOUND);
                    }
                })
                .toList();

        log.info("최근 키워드 검색 결과 {}개 반환: {}", result.size(), email);
        return result;
    }

    //3.검색 화면: 검색어를 입력하면 검색어에 맞는 장소를 보여주면서 키워드나 장소를 저장하는 기능-get+URI Param/searchAndSavePlaces(/api/route/searchs)
    @Transactional
    public List<RouteResponse.CurrentPlaceDto> searchAndSavePlaces(String email, String keyword, boolean save) {
        log.debug("email:{}, keyword: {}, save:{}", email, keyword, save);

        //요청한다음에 KakaoApiResponseDto 전체 카카오 응답 받기(여기서 keyword 사용)
        KakaoApiResponseDto kakaoApiResponseDto = kakaoFeignClient.searchByKeyword("KakaoAK " + kakaoKey, keyword);
        log.info("장소 검색 결과: {}개", kakaoApiResponseDto.getMeta().getPageableCount());

        //우리 DB에 저장된 장소이면 isRegistered를 true로 바꾼후 프론트에 전달할 형식으로 변경
        //1. 카카오 응답에서 ID만 추출
        List<String> kakaoPlaceIds = kakaoApiResponseDto.getDocuments().stream()
                .map(KakaoApiResponseDto.DocumentDto::getId)
                .toList();
        //2. DB에 존재하는 ID들을 한번에 조회
        Set<String> registeredIds = placeRepository.findAllByKakaoIdIn(kakaoPlaceIds).stream()
                .map(Place::getKakaoId)
                .collect(Collectors.toSet());
        //3. 비교하여 ture/flase list 생성
        List<Boolean> isRegistereds = kakaoPlaceIds.stream()
                .map(registeredIds::contains)
                .toList();
        //return 해줄 List dto 생성
        List<RouteResponse.CurrentPlaceDto> result = IntStream.range(0, kakaoApiResponseDto.getDocuments().size())
                .mapToObj(i -> RouteResponse.CurrentPlaceDto.from(
                        kakaoApiResponseDto.getDocuments().get(i),
                        isRegistereds.get(i)
                ))
                .toList();


        if (save) {//save=true이면 저장하고 전송
            //저장할 recentSearch 생성
            Member.RecentSearch recentSearch = Member.RecentSearch.builder()
                    .id(UUID.randomUUID().toString())
                    .keyword(keyword)
                    .searchedAt(LocalDateTime.now())
                    .build();
            //최대 20개만 저장하는 로직
            //MongoTempalte: 실행 엔진 계층으로 직접적으로 MongoDB에 BSON을 전송
            Query query = new Query(Criteria.where("email").is(email));
            Update update = new Update().push("recentSearches") //Member 엔티티의 필드명
                    .atPosition(0) //0번 인덱스에 삽입(최신순)
                    .slice(20) //배열의 크기를 20개로 유지
                    .each(recentSearch); //삽입할 데이터
            UpdateResult resultCount = mongoTemplate.updateFirst(query, update, Member.class);
            if (resultCount.getMatchedCount() == 0) {
                log.error("사용자를 찾을 수 없습니다.: {}", email);
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
            }
            log.info("장소 검색 결과 저장 완료: {}", email);
            return result;
        }
        //save=false이면 저장하지 않고 dto로 전송
        return result;
    }

    //4.길찾기 조회 완료 화면: 출발지와 목적지를 입력하고 '길찾기'를 누르면 그에 맞는 경로들을 보여주면서 저장하는 기능-POST/findAndSavePaths(/api/route/paths)
    @Transactional
//    public RouteResponse.CurrentRouteDto findAndSaveRoutes(String email, RouteRequest.RouteSearchRequest
//            routeSearchRequest) {
    public OdsayRouteSearchResponse findAndSaveRoutes(String email, RouteRequest.RouteSearchRequest
            routeSearchRequest) {
        log.info("{} -> {} ODsay 길찾기 API 요청", routeSearchRequest.departureName(), routeSearchRequest.arrivalName());
        OdsayRouteSearchResponse odsayResponse = odsayClient.searchRoute(
                routeSearchRequest.sx(), routeSearchRequest.sy(),
                routeSearchRequest.ex(), routeSearchRequest.ey(), apiKey);
        //비정상 흐름 처리
        if (odsayResponse.error() != null) {
            log.warn("ODsay 길찾기 에러: {},에러코드={}", email, odsayResponse.error().code());
            handleOdsayError(odsayResponse.error().code());
        }
        if (odsayResponse.result() == null || odsayResponse.result().path() == null) {
            log.info("ODsay 경로 탐색 결과 없음 (path is null)");
            throw new CustomException(ErrorCode.NO_SEARCH_RESULT);
        }

        log.info("ODsay 길찾기 정상 응답");

        //경로 저장 로직
        GeoJsonPoint departureLocation = new GeoJsonPoint(Double.parseDouble(routeSearchRequest.sx()), Double.parseDouble(routeSearchRequest.sy()));
        GeoJsonPoint arrivalLocation = new GeoJsonPoint(Double.parseDouble(routeSearchRequest.ex()), Double.parseDouble(routeSearchRequest.ey()));

        Member.RecentRoute recentRoute = Member.RecentRoute.builder()
                .id(UUID.randomUUID().toString())
                .departureName(routeSearchRequest.departureName())
                .arrivalName(routeSearchRequest.arrivalName())
                .departureLocation(departureLocation)
                .arrivalLocation(arrivalLocation)
                .searchedAt(LocalDateTime.now())
                .build();

        //저장 로직MongoTemplate(최대 20개)
        //MongoTempalte: 실행 엔진 계층으로 직접적으로 MongoDB에 BSON을 전송
        Query query = new Query(Criteria.where("email").is(email));

        Update update = new Update().push("recentRoutes") //Member 엔티티의 필드명
                .atPosition(0) //0번 인덱스에 삽입(최신순)
                .slice(20) //배열의 크기를 20개로 유지
                .each(recentRoute); //삽입할 데이터

        UpdateResult result = mongoTemplate.updateFirst(query, update, Member.class);
        if (result.getMatchedCount() == 0) {
            log.error("사용자를 찾을 수 없습니다.: {}", email);
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        log.info("경로 기록 저장 완료: {}", email);

        //return RouteResponse.CurrentRouteDto.from(odsayResponse);
        return odsayResponse;
    }

    //길찾기 기록 제거 서비스 로직
    @Transactional
    public void deleteRecentRoute(String email, String recentRouteId) {
        int isRemoved = memberRepository.deleteRecentRouteByEmail(email,recentRouteId);
        if(isRemoved == 0) {
            throw new CustomException(ErrorCode.ROUTE_HISTORY_NOT_FOUND);
        }
        log.info("길찾기 기록(ID:{}) 제거 완료", recentRouteId);
    }

    //키워드 검색 기록 제거 서비스 로직
    @Transactional
    public void deleteRecentSearch(String email, String recentSearchId) {
        int isRemoved = memberRepository.deleteRecentSearchByEmail(email,recentSearchId);
        if(isRemoved == 0) {
            throw new CustomException(ErrorCode.SEARCH_HISTORY_NOT_FOUND);
        }
        log.info("키워드 검색 기록(ID:{}) 제거 완료", recentSearchId);
    }

    // ODsay 에러 코드 핸들링 내부 method
    private void handleOdsayError(String odsayErrorCode) {
        throw switch (odsayErrorCode) {
            case "500" -> new CustomException(ErrorCode.ODSAY_SERVER_ERROR);
            case "-8", "-9" -> new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            case "3", "4", "5" -> new CustomException(ErrorCode.STATION_NOT_FOUND);
            case "6" -> new CustomException(ErrorCode.NOT_A_SERVICE_AREA);
            case "-98" -> new CustomException(ErrorCode.DISTANCE_TOO_SHORT);
            case "-99" -> new CustomException(ErrorCode.NO_SEARCH_RESULT);
            default -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Odsay 요청 중 예상하지 못한 예외 발생");
        };
    }


}
