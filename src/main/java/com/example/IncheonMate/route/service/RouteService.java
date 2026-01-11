package com.example.IncheonMate.route.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RouteService {

    private final OdsayClient odsayClient;
    private final MemberRepository memberRepository;
    private final MongoTemplate mongoTemplate;//기록 개수 제한을 위해서 MongoTempalte를 사용

    @Value("${ODSAY_KEY}")
    private String apiKey;

//    public Map<String, Object> getRoute(){
//        log.info("요청함");
//        Map<String, Object> response = odsayClient.searchRoute(
//                "126.9027279","37.5349277",
//                "126.9145430","37.5499421",
//                apiKey);
//
//        return response;
//    }

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

        //요청한다음에 KakaoMapKeywordSearchResponse로 전체 카카오 응답 받기(여기서 keyword 사용)
        //KakaoMapKeywordSearchResponse kakaoResponse = xxxxxxxxService.xxxxxxxxxx(keyword);
        //if(save) {//save=true이면 저장하고 전송
        //log.info("장소 검색 결과: {}",kakaoRespnse.getxxxx.getxxxxx.size());
        //Member.RecentSearch recentSearch = Member.RecentSearch.builder().id(UUID.randomUUID().toString()).title().type().searchedAt(LocalDateTime.now());
        //저장 로직MongoTemplate(최대 20개)
        //MongoTempalte: 실행 엔진 계층으로 직접적으로 MongoDB에 BSON을 전송
        //Query query = new Query(Criteria.where("email").is(email));

        //Update update = new Update().push("recentPlaces") //Member 엔티티의 필드명
//                .atPosition(0) //0번 인덱스에 삽입(최신순)
//                .slice(20) //배열의 크기를 20개로 유지
//                .each(recentSearch); //삽입할 데이터
//
//        mongoTemplate.updateFirst(query,update,Member.class);
        //log.info("장소 검색 결과 저장 완료: {}" ,email);
        //return RouteResponse.CurrentPlaceDto.from(kakaoResponse.document());
        //}
        //save=false이면 저장하지 않고 dto로 전송
        //return RouteResponse.CurrentPlaceDto.from(kakaoResponse.document());

        return null;
    }

    //4.길찾기 조회 완료 화면: 출발지와 목적지를 입력하고 '길찾기'를 누르면 그에 맞는 경로들을 보여주면서 저장하는 기능-POST/findAndSavePaths(/api/route/paths)
    @Transactional
    public RouteResponse.CurrentRouteDto findAndSaveRoutes(String email, RouteRequest.RouteSearchRequest
            routeSearchRequest) {
        log.info("{} -> {} ODsay 길찾기 API 요청", routeSearchRequest.departureName(), routeSearchRequest.arrivalName());
        OdsayRouteSearchResponse odsayResponse = odsayClient.searchRoute(
                routeSearchRequest.sx(), routeSearchRequest.sy(),
                routeSearchRequest.ex(), routeSearchRequest.ey(), apiKey);
        //비정상 흐름 처리
        if(odsayResponse.error() != null){
            log.warn("ODsay 길찾기 에러: {},에러코드={}",email,odsayResponse.error().code());
            handleOdsayError(odsayResponse.error().code());
        }
        if(odsayResponse.result() == null || odsayResponse.result().path() == null){
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
        if(result.getMatchedCount() == 0){
            log.error("사용자를 찾을 수 없습니다.: {}",email);
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        log.info("경로 기록 저장 완료: {}", email);

        return RouteResponse.CurrentRouteDto.from(odsayResponse);
    }


    //길찾기 조회 데이터 선택을 위한 임시 서비스
    public OdsayRouteSearchResponse getWholeOdsay(String sx, String sy, String ex, String ey) {
        return odsayClient.searchRoute(sx, sy, ex, ey, apiKey);
    }

    // ODsay 에러 코드 핸들링 내부 method
    private void handleOdsayError(String odsayErrorCode){
        throw switch (odsayErrorCode){
            case "500" -> new CustomException(ErrorCode.ODSAY_SERVER_ERROR);
            case "-8","-9" -> new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            case "3","4","5" -> new CustomException(ErrorCode.STATION_NOT_FOUND);
            case "6" -> new CustomException(ErrorCode.NOT_A_SERVICE_AREA);
            case "-98" -> new CustomException(ErrorCode.DISTANCE_TOO_SHORT);
            case "-99" -> new CustomException(ErrorCode.NO_SEARCH_RESULT);
            default -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,"Odsay 요청 중 예상하지 못한 예외 발생");
        };
    }
}
