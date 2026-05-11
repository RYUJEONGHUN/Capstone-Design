package com.example.IncheonMate.place.domain;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.example.IncheonMate.place.domain.type.PlaceCategory;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "place") // MongoDB 컬렉션 이름 지정
public class Place {

    @Id // MongoDB의 _id (자동 생성되는 문자열 키)
    private String id;

    // 🔑 핵심: 카카오 장소 ID (검색용 인덱스 걸기)
    @Indexed(unique = true)
    private String kakaoId;

    // --- 📝 기본 정보 ---
    private String name;
    private String address;
    private PlaceCategory placeCategory;

    // 좌표
    private Double x;
    private Double y;

    // --- 우리만의 고유 데이터 ---
    private String expertComment; // 한 줄 평
    private Double ourRating;      // 자체 별점
    private String thumbnailUrl;   // 사진 URL

    // --- 내기프트 연동---
    private String naegiftId;

    // 리스트 그대로 저장
    private List<String> tags = new ArrayList<>();

    @Builder
    public Place(String kakaoId, String name, String address, PlaceCategory placeCategory, Double x, Double y, String expertComment, Double ourRating, String thumbnailUrl, List<String> tags,String naegiftId) {
        this.kakaoId = kakaoId;
        this.name = name;
        this.address = address;
        this.placeCategory = placeCategory;
        this.x = x;
        this.y = y;
        this.expertComment = expertComment;
        this.naegiftId = naegiftId;
        this.ourRating = ourRating != null ? ourRating : 0.0;
        this.thumbnailUrl = thumbnailUrl;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    // 데이터 덮어쓰기 메서드
    public void updateMyData(Double rating, List<String> tags, String imageUrl, String comment) {
        this.ourRating = rating;
        this.tags = tags;
        this.thumbnailUrl = imageUrl;
        this.expertComment = comment;
    }

    public CoursePlaceCategory getCoursePlaceCategory() {
        switch (this.getPlaceCategory()){
            case AT4:
                return CoursePlaceCategory.TOURIST_ATTRACTION;
            case CT1:
                return CoursePlaceCategory.TOURIST_ATTRACTION;
            case FD6:
                return CoursePlaceCategory.RESTAURANT;
        }
        return null;
    }
}