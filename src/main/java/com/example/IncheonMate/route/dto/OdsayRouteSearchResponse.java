package com.example.IncheonMate.route.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "ODsay 대중교통 길찾기 API 전체 응답")
public record OdsayRouteSearchResponse(
        @Schema(description = "검색 결과 데이터")
        @JsonProperty("result") Result result,

        @Schema(description = "에러 정보 (정상 응답 시 null)")
        @JsonProperty("error")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        ErrorInfo error
) {
    @Schema(description = "ODsay 검색 결과 상세")
    public record Result(
            @Schema(description = "결과 구분 (0:도시내, 1:도시간 직통, 2:도시간 환승)", example = "0")
            @JsonProperty("searchType") Integer searchType,

            @Schema(description = "도시간 직통 탐색 결과 유무 (0:False, 1:True)", example = "0")
            @JsonProperty("outTrafficCheck") Integer outTrafficCheck,

            @Schema(description = "버스 경로 개수", example = "2")
            @JsonProperty("busCount") Integer busCount,

            @Schema(description = "지하철 경로 개수", example = "1")
            @JsonProperty("subwayCount") Integer subwayCount,

            @Schema(description = "버스+지하철 복합 경로 개수", example = "3")
            @JsonProperty("subwayBusCount") Integer subwayBusCount,

            @Schema(description = "출발지와 도착지 직선 거리 (미터)", example = "5000.5")
            @JsonProperty("pointDistance") Double pointDistance,

            @Schema(description = "출발지 반경", example = "700")
            @JsonProperty("startRadius") Integer startRadius,

            @Schema(description = "도착지 반경", example = "700")
            @JsonProperty("endRadius") Integer endRadius,

            @ArraySchema(schema = @Schema(description = "탐색된 경로 리스트", implementation = Path.class))
            @JsonProperty("path") List<Path> path
    ) {}

    @Schema(description = "개별 경로 정보")
    public record Path(
            @Schema(description = "경로 종류 (1:지하철, 2:버스, 3:버스+지하철)", example = "3")
            @JsonProperty("pathType") Integer pathType,

            @Schema(description = "경로 요약 정보")
            @JsonProperty("info") Info info,

            @ArraySchema(schema = @Schema(description = "이동 교통 수단 상세 리스트", implementation = SubPath.class))
            @JsonProperty("subPath") List<SubPath> subPath
    ) {}

    @Schema(description = "경로 요약 정보")
    public record Info(
            @Schema(description = "도보를 제외한 총 이동 거리(m)", example = "4500.0")
            @JsonProperty("trafficDistance") Double trafficDistance,

            @Schema(description = "총 도보 이동 거리(m)", example = "300")
            @JsonProperty("totalWalk") Integer totalWalk,

            @Schema(description = "총 소요시간(분)", example = "45")
            @JsonProperty("totalTime") Integer totalTime,

            @Schema(description = "총 요금(원)", example = "1250")
            @JsonProperty("payment") Integer payment,

            @Schema(description = "버스 환승 횟수", example = "1")
            @JsonProperty("busTransitCount") Integer busTransitCount,

            @Schema(description = "지하철 환승 횟수", example = "0")
            @JsonProperty("subwayTransitCount") Integer subwayTransitCount,

            @Schema(description = "보간점 API 파라미터 값 (맵 그리기용)", hidden = true)
            @JsonProperty("mapObj") String mapObj,

            @Schema(description = "최초 출발 정류장/역", example = "인천대입구")
            @JsonProperty("firstStartStation") String firstStartStation,

            @Schema(description = "최초 출발 정류장/역 (국문)", example = "인천대입구")
            @JsonProperty("firstStartStationKor") String firstStartStationKor,

            @Schema(description = "최초 출발 정류장/역 (일문)", hidden = true)
            @JsonProperty("firstStartStationJpnKata") String firstStartStationJpnKata,

            @Schema(description = "최종 도착 정류장/역", example = "부평역")
            @JsonProperty("lastEndStation") String lastEndStation,

            @Schema(description = "최종 도착 정류장/역 (국문)", example = "부평역")
            @JsonProperty("lastEndStationKor") String lastEndStationKor,

            @Schema(description = "최종 도착 정류장/역 (일문)", hidden = true)
            @JsonProperty("lastEndStationJpnKata") String lastEndStationJpnKata,

            @Schema(description = "총 정류장 합계", example = "15")
            @JsonProperty("totalStationCount") Integer totalStationCount,

            @Schema(description = "버스 정류장 합계", example = "5")
            @JsonProperty("busStationCount") Integer busStationCount,

            @Schema(description = "지하철 정류장 합계", example = "10")
            @JsonProperty("subwayStationCount") Integer subwayStationCount,

            @Schema(description = "총 거리(m)", example = "4800.0")
            @JsonProperty("totalDistance") Double totalDistance,

            @Schema(description = "배차간격 체크 기준 시간(분)", example = "10")
            @JsonProperty("checkIntervalTime") Integer checkIntervalTime,

            @Schema(description = "배차간격 초과 노선 존재 여부(Y/N)", example = "N")
            @JsonProperty("checkIntervalTimeOverYn") String checkIntervalTimeOverYn,

            @Schema(description = "전체 배차간격 시간(분)", example = "15")
            @JsonProperty("totalIntervalTime") Integer totalIntervalTime
    ) {}

    @Schema(description = "경로 상세 구간 정보 (이동 수단별)")
    public record SubPath(
            @Schema(description = "이동 수단 종류 (1:지하철, 2:버스, 3:도보)", example = "2")
            @JsonProperty("trafficType") Integer trafficType,

            @Schema(description = "구간 이동 거리(m)", example = "1200.0")
            @JsonProperty("distance") Double distance,

            @Schema(description = "구간 소요 시간(분)", example = "15")
            @JsonProperty("sectionTime") Integer sectionTime,

            @Schema(description = "이동하여 정차하는 정거장 수", example = "6")
            @JsonProperty("stationCount") Integer stationCount,

            @ArraySchema(schema = @Schema(description = "교통 노선 정보 리스트", implementation = Lane.class))
            @JsonProperty("lane") List<Lane> lane,

            @Schema(description = "평균 배차간격(분)", example = "8")
            @JsonProperty("intervalTime") Integer intervalTime,

            @Schema(description = "승차 정류장/역명", example = "센트럴파크")
            @JsonProperty("startName") String startName,

            @Schema(description = "승차 정류장/역명(국문)", example = "센트럴파크")
            @JsonProperty("startNameKor") String startNameKor,

            @Schema(hidden = true)
            @JsonProperty("startNameJpnKata") String startNameJpnKata,

            @Schema(description = "승차 정류장 X좌표", example = "126.639")
            @JsonProperty("startX") Double startX,

            @Schema(description = "승차 정류장 Y좌표", example = "37.393")
            @JsonProperty("startY") Double startY,

            @Schema(description = "하차 정류장/역명", example = "캠퍼스타운")
            @JsonProperty("endName") String endName,

            @Schema(description = "하차 정류장/역명(국문)", example = "캠퍼스타운")
            @JsonProperty("endNameKor") String endNameKor,

            @Schema(hidden = true)
            @JsonProperty("endNameJpnKata") String endNameJpnKata,

            @Schema(description = "하차 정류장 X좌표", example = "126.656")
            @JsonProperty("endX") Double endX,

            @Schema(description = "하차 정류장 Y좌표", example = "37.387")
            @JsonProperty("endY") Double endY,

            @Schema(description = "방면 정보", example = "계양 방면")
            @JsonProperty("way") String way,

            @Schema(description = "방면 정보 코드 (1:상행, 2:하행)", example = "1")
            @JsonProperty("wayCode") Integer wayCode,

            @Schema(description = "지하철 빠른 환승 위치", example = "1-1")
            @JsonProperty("door") String door,

            @Schema(description = "출발 정류장/역 ID (ODsay 내부 코드)", example = "1001")
            @JsonProperty("startID") Integer startID,

            @Schema(description = "출발 정류장 도시코드", example = "1000")
            @JsonProperty("startStationCityCode") Integer startStationCityCode,

            @Schema(description = "출발 정류장 BIS 코드", example = "1111")
            @JsonProperty("startStationProviderCode") Integer startStationProviderCode,

            @Schema(description = "각 지역 출발 정류장 ID", example = "1650001")
            @JsonProperty("startLocalStationID") String startLocalStationID,

            @Schema(description = "각 지역 출발 정류장 고유번호(ARS-ID)", example = "01001")
            @JsonProperty("startArsID") String startArsID,

            @Schema(description = "도착 정류장/역 ID", example = "1002")
            @JsonProperty("endID") Integer endID,

            @Schema(description = "도착 정류장 도시코드", example = "1000")
            @JsonProperty("endStationCityCode") Integer endStationCityCode,

            @Schema(description = "도착 정류장 BIS 코드", example = "2222")
            @JsonProperty("endStationProviderCode") Integer endStationProviderCode,

            @Schema(description = "각 지역 도착 정류장 ID", example = "1650002")
            @JsonProperty("endLocalStationID") String endLocalStationID,

            @Schema(description = "각 지역 도착 정류장 고유번호(ARS-ID)", example = "01002")
            @JsonProperty("endArsID") String endArsID,

            @Schema(description = "지하철 진입 출구 번호", example = "3")
            @JsonProperty("startExitNo") String startExitNo,

            @Schema(description = "지하철 진입 출구 X좌표", example = "126.123")
            @JsonProperty("startExitX") Double startExitX,

            @Schema(description = "지하철 진입 출구 Y좌표", example = "37.123")
            @JsonProperty("startExitY") Double startExitY,

            @Schema(description = "지하철 진출 출구 번호", example = "1")
            @JsonProperty("endExitNo") String endExitNo,

            @Schema(description = "지하철 진출 출구 X좌표", example = "126.124")
            @JsonProperty("endExitX") Double endExitX,

            @Schema(description = "지하철 진출 출구 Y좌표", example = "37.124")
            @JsonProperty("endExitY") Double endExitY,

            @Schema(description = "경유 정류장 목록 확장 노드")
            @JsonProperty("passStopList") PassStopList passStopList
    ) {}

    @Schema(description = "교통 노선 정보")
    public record Lane(
            @Schema(description = "지하철 노선명", example = "인천 1호선")
            @JsonProperty("name") String name,

            @Schema(description = "지하철 노선명(국문)", example = "인천 1호선")
            @JsonProperty("nameKor") String nameKor,

            @Schema(hidden = true)
            @JsonProperty("nameJpnKata") String nameJpnKata,

            @Schema(description = "버스 번호", example = "8")
            @JsonProperty("busNo") String busNo,

            @Schema(description = "버스 번호(국문)", example = "8")
            @JsonProperty("busNoKor") String busNoKor,

            @Schema(hidden = true)
            @JsonProperty("busNoJpnKata") String busNoJpnKata,

            @Schema(description = "버스 타입 (1:일반, 2:좌석 등)", example = "1")
            @JsonProperty("type") Integer type,

            @Schema(description = "버스 ID", example = "5001")
            @JsonProperty("busID") Integer busID,

            @Schema(description = "각 지역 버스노선 ID", example = "1651001")
            @JsonProperty("busLocalBlID") String busLocalBlID,

            @Schema(description = "운수회사 승인 도시코드", example = "1000")
            @JsonProperty("busCityCode") Integer busCityCode,

            @Schema(description = "BIS 코드", example = "9999")
            @JsonProperty("busProviderCode") Integer busProviderCode,

            @Schema(description = "지하철 노선 번호", example = "101")
            @JsonProperty("subwayCode") Integer subwayCode,

            @Schema(description = "지하철 도시코드", example = "1000")
            @JsonProperty("subwayCityCode") Integer subwayCityCode
    ) {}

    @Schema(description = "경유 정류장 정보 그룹")
    public record PassStopList(
            @ArraySchema(schema = @Schema(description = "정류장 목록", implementation = Station.class))
            @JsonProperty("stations") List<Station> stations
    ) {}

    @Schema(description = "개별 정류장 정보")
    public record Station(
            @Schema(description = "정류장 순번", example = "1")
            @JsonProperty("index") Integer index,

            @Schema(description = "정류장 ID", example = "1001")
            @JsonProperty("stationID") Integer stationID,

            @Schema(description = "정류장 명칭", example = "테크노파크")
            @JsonProperty("stationName") String stationName,

            @Schema(description = "정류장 명칭(국문)", example = "테크노파크")
            @JsonProperty("stationNameKor") String stationNameKor,

            @Schema(hidden = true)
            @JsonProperty("stationNameJpnKata") String stationNameJpnKata,

            @Schema(description = "정류장 도시코드", example = "1000")
            @JsonProperty("stationCityCode") Integer stationCityCode,

            @Schema(description = "BIS 코드", example = "1234")
            @JsonProperty("stationProviderCode") Integer stationProviderCode,

            @Schema(description = "각 지역 정류장 ID", example = "1230001")
            @JsonProperty("localStationID") String localStationID,

            @Schema(description = "각 지역 정류장 고유번호(ARS-ID)", example = "01005")
            @JsonProperty("arsID") String arsID,

            @Schema(description = "정류장 X좌표", example = "126.64")
            @JsonProperty("x") String x,

            @Schema(description = "정류장 Y좌표", example = "37.38")
            @JsonProperty("y") String y,

            @Schema(description = "미정차 정류장 여부(Y/N)", example = "N")
            @JsonProperty("isNonStop") String isNonStop
    ) {}

    @Schema(description = "에러 정보")
    public record ErrorInfo(
            @Schema(description = "에러 메시지", example = "검색 결과가 없습니다.")
            @JsonProperty("msg") String message,

            @Schema(description = "에러 코드", example = "-99")
            @JsonProperty("code") String code
    ){}
}