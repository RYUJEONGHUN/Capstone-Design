package com.example.IncheonMate.course.domain;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.mongodb.lang.Nullable;
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
@Document(collection = "curated_courses")
public class CuratedCourse {

    @Id
    private String id;

    private String title;
    private boolean isVisible;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<CuratedSpot> spots = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CuratedSpot{
        private int spotOrder;
        private String kakaoId;
        private String placeId;
        @Nullable
        private String naegiftId;
        private String name;
        private String address;
        private CoursePlaceCategory coursePlaceCategory;
        private String thumbnailUrl;
        private String expertComment;
        private GeoJsonPoint geoJsonPoint;
    }
}
