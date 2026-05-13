package com.example.IncheonMate.reward.domain;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Document(collection = "reward_course")
public class RewardCourse {

    @Id
    private String id;

    private String title;
    private boolean isVisible;

    private String rewardComment;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    //버전 관리 기능 필요시 주석 해제
    //@Version private Long version; 

    @Builder.Default
    private List<RewardSpot> rewardSpots = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RewardSpot{
        private int spotOrder;
        private String kakaoId;
        private String placeId;
        private String naegiftId;
        private String name;
        private String address;
        private CoursePlaceCategory coursePlaceCategory;
        private String thumbnailUrl;
        private String expertComment;
        private GeoJsonPoint geoJsonPoint;
    }

}

