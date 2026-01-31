package com.example.IncheonMate.curation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Map;

@Document(collection = "curation_spots")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurationSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String placeId;      // 원본 Place ID
    private String placeName;    // 장소 이름 (팝업 제목용)

    private String kakaoId; // 카카오 ID

    //  지오펜싱 데이터 (앱이 계산할 기준)
    private Double lat;          // 위도
    private Double lng;          // 경도
    private int triggerRadius;   // 감지 반경 (m) - 예: 50m

    //  AI 페르소나 멘트 (미리 생성됨)
    // Key: 성향 (INFP, 소음인), Value: 멘트
    private Map<String, String> aiComments;

    private boolean isActive;    // 팝업 활성화 여부
}