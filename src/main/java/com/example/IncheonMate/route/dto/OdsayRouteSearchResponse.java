package com.example.IncheonMate.route.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OdsayRouteSearchResponse(
        /** 1. 데이터를 포함하는 최 상위 노드 */
        @JsonProperty("result") Result result
) {

    public record Result(
            /** 1-1. 결과 구분 (0:도시내, 1:도시간 직통, 2:도시간 환승) */
            @JsonProperty("searchType") Integer searchType,

            /** 1-2. 도시간 "직통" 탐색 결과 유무(환승 X) (0:False, 1:True) */
            @JsonProperty("outTrafficCheck") Integer outTrafficCheck,

            /** 1-3. 버스 결과 개수 */
            @JsonProperty("busCount") Integer busCount,

            /** 1-4. 지하철 결과 개수 */
            @JsonProperty("subwayCount") Integer subwayCount,

            /** 1-5. “버스+지하철” 결과 개수 */
            @JsonProperty("subwayBusCount") Integer subwayBusCount,

            /** 1-6. 출발지(SX, SY)와 도착지(EX, EY)의 직선 거리 (미터) */
            @JsonProperty("pointDistance") Double pointDistance,

            /** 1-7. 출발지 반경 */
            @JsonProperty("startRadius") Integer startRadius,

            /** 1-8. 도착지 반경 */
            @JsonProperty("endRadius") Integer endRadius,

            /** 1-9. 결과 리스트 확장 노드 */
            @JsonProperty("path") List<Path> path
    ) {}

    public record Path(
            /** 1-9-1. 결과 종류 (1:지하철, 2:버스, 3:버스+지하철) */
            @JsonProperty("pathType") Integer pathType,

            /** 1-9-2. 요약 정보 확장 노드 */
            @JsonProperty("info") Info info,

            /** 1-9-3. 이동 교통 수단 정보 확장 노드 */
            @JsonProperty("subPath") List<SubPath> subPath
    ) {}

    public record Info(
            /** 1-9-2-1. 도보를 제외한 총 이동 거리 */
            @JsonProperty("trafficDistance") Double trafficDistance,

            /** 1-9-2-2. 총 도보 이동 거리 */
            @JsonProperty("totalWalk") Integer totalWalk,

            /** 1-9-2-3. 총 소요시간 */
            @JsonProperty("totalTime") Integer totalTime,

            /** 1-9-2-4. 총 요금 */
            @JsonProperty("payment") Integer payment,

            /** 1-9-2-5. 버스 환승 카운트 */
            @JsonProperty("busTransitCount") Integer busTransitCount,

            /** 1-9-2-6. 지하철 환승 카운트 */
            @JsonProperty("subwayTransitCount") Integer subwayTransitCount,

            /** 1-9-2-7. 보간점 API를 호출하기 위한 파라미터 값 */
            @JsonProperty("mapObj") String mapObj,

            /** 1-9-2-8. 최초 출발역/정류장 */
            @JsonProperty("firstStartStation") String firstStartStation,

            /** 1-9-2-9. 최초 출발역/정류장 국문 */
            @JsonProperty("firstStartStationKor") String firstStartStationKor,

            /** 1-9-2-10. 최초 출발역/정류장 일문(가타카나) */
            @JsonProperty("firstStartStationJpnKata") String firstStartStationJpnKata,

            /** 1-9-2-11. 최종 도착역/정류장 */
            @JsonProperty("lastEndStation") String lastEndStation,

            /** 1-9-2-12. 최종 도착역/정류장 국문 */
            @JsonProperty("lastEndStationKor") String lastEndStationKor,

            /** 1-9-2-13. 최종 도착역/정류장 일문(가타카나) */
            @JsonProperty("lastEndStationJpnKata") String lastEndStationJpnKata,

            /** 1-9-2-14. 총 정류장 합 */
            @JsonProperty("totalStationCount") Integer totalStationCount,

            /** 1-9-2-15. 버스 정류장 합 */
            @JsonProperty("busStationCount") Integer busStationCount,

            /** 1-9-2-16. 지하철 정류장 합 */
            @JsonProperty("subwayStationCount") Integer subwayStationCount,

            /** 1-9-2-17. 총 거리 */
            @JsonProperty("totalDistance") Double totalDistance,

            /** 1-9-2-18. 배차간격 체크 기준 시간(분) */
            @JsonProperty("checkIntervalTime") Integer checkIntervalTime,

            /** 1-9-2-19. 배차간격 체크 기준시간을 초과하는 노선이 존재하는지 여부(Y/N) */
            @JsonProperty("checkIntervalTimeOverYn") String checkIntervalTimeOverYn,

            /** 1-9-2-20. 전체 배차간격 시간(분) */
            @JsonProperty("totalIntervalTime") Integer totalIntervalTime
    ) {}

    public record SubPath(
            /** 1-9-3-1. 이동 수단 종류 (1:지하철, 2:버스, 3:도보) */
            @JsonProperty("trafficType") Integer trafficType,

            /** 1-9-3-2. 이동 거리 */
            @JsonProperty("distance") Double distance,

            /** 1-9-3-3. 이동 소요 시간 */
            @JsonProperty("sectionTime") Integer sectionTime,

            /** 1-9-3-4. 이동하여 정차하는 정거장 수 */
            @JsonProperty("stationCount") Integer stationCount,

            /** 1-9-3-5. 교통 수단 정보 확장 노드 */
            @JsonProperty("lane") List<Lane> lane,

            /** 1-9-3-6. 평균 배차간격(분) */
            @JsonProperty("intervalTime") Integer intervalTime,

            /** 1-9-3-7. 승차 정류장/역명 */
            @JsonProperty("startName") String startName,

            /** 1-9-3-8. 승차 정류장/역명 국문 */
            @JsonProperty("startNameKor") String startNameKor,

            /** 1-9-3-9. 승차 정류장/역명 일문(가타카나) */
            @JsonProperty("startNameJpnKata") String startNameJpnKata,

            /** 1-9-3-10. 승차 정류장/역 X 좌표 */
            @JsonProperty("startX") Double startX,

            /** 1-9-3-11. 승차 정류장/역 Y 좌표 */
            @JsonProperty("startY") Double startY,

            /** 1-9-3-12. 하차 정류장/역명 */
            @JsonProperty("endName") String endName,

            /** 1-9-3-13. 하차 정류장/역명 국문 */
            @JsonProperty("endNameKor") String endNameKor,

            /** 1-9-3-14. 하차 정류장/역명 일문(가타카나) */
            @JsonProperty("endNameJpnKata") String endNameJpnKata,

            /** 1-9-3-15. 하차 정류장/역 X 좌표 */
            @JsonProperty("endX") Double endX,

            /** 1-9-3-16. 하차 정류장/역 Y 좌표 */
            @JsonProperty("endY") Double endY,

            /** 1-9-3-17. 방면 정보 */
            @JsonProperty("way") String way,

            /** 1-9-3-18. 방면 정보 코드 (1:상행, 2:하행) */
            @JsonProperty("wayCode") Integer wayCode,

            /** 1-9-3-19. 지하철 빠른 환승 위치 */
            @JsonProperty("door") String door,

            /** 1-9-3-20. 출발 정류장/역 코드 */
            @JsonProperty("startID") Integer startID,

            /** 1-9-3-21. 출발 정류장 도시코드 */
            @JsonProperty("startStationCityCode") Integer startStationCityCode,

            /** 1-9-3-22. 출발 정류장 BIS 코드 */
            @JsonProperty("startStationProviderCode") Integer startStationProviderCode,

            /** 1-9-3-23. 각 지역 출발 정류장 ID */
            @JsonProperty("startLocalStationID") String startLocalStationID,

            /** 1-9-3-24. 각 지역 출발 정류장 고유번호 */
            @JsonProperty("startArsID") String startArsID,

            /** 1-9-3-25. 도착 정류장/역 코드 */
            @JsonProperty("endID") Integer endID,

            /** 1-9-3-26. 도착 정류장 도시코드 */
            @JsonProperty("endStationCityCode") Integer endStationCityCode,

            /** 1-9-3-27. 도착 정류장 BIS 코드 */
            @JsonProperty("endStationProviderCode") Integer endStationProviderCode,

            /** 1-9-3-28. 각 지역 도착 정류장 ID */
            @JsonProperty("endLocalStationID") String endLocalStationID,

            /** 1-9-3-29. 각 지역 도착 정류장 고유번호 */
            @JsonProperty("endArsID") String endArsID,

            /** 1-9-3-30. 지하철 들어가는 출구 번호 */
            @JsonProperty("startExitNo") String startExitNo,

            /** 1-9-3-31. 지하철 들어가는 출구 X좌표 */
            @JsonProperty("startExitX") Double startExitX,

            /** 1-9-3-32. 지하철 들어가는 출구 Y좌표 */
            @JsonProperty("startExitY") Double startExitY,

            /** 1-9-3-33. 지하철 나가는 출구 번호 */
            @JsonProperty("endExitNo") String endExitNo,

            /** 1-9-3-34. 지하철 나가는 출구 X좌표 */
            @JsonProperty("endExitX") Double endExitX,

            /** 1-9-3-35. 지하철 나가는 출구 Y좌표 */
            @JsonProperty("endExitY") Double endExitY,

            /** 1-9-3-36. 경로 상세구간 정보 확장 노드 */
            @JsonProperty("passStopList") PassStopList passStopList
    ) {}

    public record Lane(
            /** 1-9-3-5-1. 지하철 노선명 */
            @JsonProperty("name") String name,

            /** 1-9-3-5-2. 지하철 노선명 국문 */
            @JsonProperty("nameKor") String nameKor,

            /** 1-9-3-5-3. 지하철 노선명 일문(가타카나) */
            @JsonProperty("nameJpnKata") String nameJpnKata,

            /** 1-9-3-5-4. 버스 번호 */
            @JsonProperty("busNo") String busNo,

            /** 1-9-3-5-5. 버스 번호 국문 */
            @JsonProperty("busNoKor") String busNoKor,

            /** 1-9-3-5-6. 버스 번호 일문(가타카나) */
            @JsonProperty("busNoJpnKata") String busNoJpnKata,

            /** 1-9-3-5-7. 버스 타입 */
            @JsonProperty("type") Integer type,

            /** 1-9-3-5-8. 버스 코드 */
            @JsonProperty("busID") Integer busID,

            /** 1-9-3-5-9. 각 지역 버스노선 ID */
            @JsonProperty("busLocalBlID") String busLocalBlID,

            /** 1-9-3-5-10. 운수회사 승인 도시코드 */
            @JsonProperty("busCityCode") Integer busCityCode,

            /** 1-9-3-5-11. BIS 코드 */
            @JsonProperty("busProviderCode") Integer busProviderCode,

            /** 1-9-3-5-12. 지하철 노선 번호 */
            @JsonProperty("subwayCode") Integer subwayCode,

            /** 1-9-3-5-13. 지하철 도시코드 */
            @JsonProperty("subwayCityCode") Integer subwayCityCode
    ) {}

    public record PassStopList(
            /** 1-9-3-36-1. 정류장 정보 그룹노드 */
            @JsonProperty("stations") List<Station> stations
    ) {}

    public record Station(
            /** 1-9-3-36-1-1. 정류장 순번 */
            @JsonProperty("index") Integer index,

            /** 1-9-3-36-1-2. 정류장 ID */
            @JsonProperty("stationID") Integer stationID,

            /** 1-9-3-36-1-3. 정류장 명칭 */
            @JsonProperty("stationName") String stationName,

            /** 1-9-3-36-1-4. 정류장 명칭 국문 */
            @JsonProperty("stationNameKor") String stationNameKor,

            /** 1-9-3-36-1-5. 정류장 명칭 일문(가타카나) */
            @JsonProperty("stationNameJpnKata") String stationNameJpnKata,

            /** 1-9-3-36-1-6. 정류장 도시코드 */
            @JsonProperty("stationCityCode") Integer stationCityCode,

            /** 1-9-3-36-1-7. BIS 코드 */
            @JsonProperty("stationProviderCode") Integer stationProviderCode,

            /** 1-9-3-36-1-8. 각 지역 정류장 ID */
            @JsonProperty("localStationID") String localStationID,

            /** 1-9-3-36-1-9. 각 지역 정류장 고유번호 */
            @JsonProperty("arsID") String arsID,

            /** 1-9-3-36-1-10. 정류장 X좌표 */
            @JsonProperty("x") String x,

            /** 1-9-3-36-1-11. 정류장 Y좌표 */
            @JsonProperty("y") String y,

            /** 1-9-3-36-1-12. 미정차 정류장 여부 Y/N */
            @JsonProperty("isNonStop") String isNonStop
    ) {}
}