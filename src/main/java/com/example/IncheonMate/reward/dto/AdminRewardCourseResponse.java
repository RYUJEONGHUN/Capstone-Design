package com.example.IncheonMate.reward.dto;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.example.IncheonMate.reward.domain.RewardCourse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class AdminRewardCourseResponse {

    public record RewardCourseDto(
            String rewardCourseId,
            String title,
            @JsonProperty("isVisible") boolean isVisible,
            String rewardDescription,
            LocalDateTime createdAt,
            List<RewardCourseSpotDto> rewardCourseSpotDtos
    ){
        public static RewardCourseDto from(RewardCourse rewardCourse){
            return new RewardCourseDto(
                    rewardCourse.getId(),
                    rewardCourse.getTitle(),
                    rewardCourse.isVisible(),
                    rewardCourse.getRewardDescription(),
                    rewardCourse.getCreatedAt(),
                    rewardCourse.getRewardSpots().stream()
                            .map(rewardSpot -> RewardCourseSpotDto.from(rewardSpot))
                            .toList()
            );
        }
    }

    public record RewardCourseSpotDto(
            int spotOrder,
            String kakaoId,
            String placeId,
            String naegiftId,
            String name,
            String address,
            CoursePlaceCategory coursePlaceCategory,
            String thumbnailUrl,
            String expertComment,
            double x,
            double y
    ){
        public static RewardCourseSpotDto from(RewardCourse.RewardSpot rewardSpot){
            return new RewardCourseSpotDto(
                    rewardSpot.getSpotOrder(),
                    rewardSpot.getKakaoId(),
                    rewardSpot.getPlaceId(),
                    rewardSpot.getNaegiftId(),
                    rewardSpot.getName(),
                    rewardSpot.getAddress(),
                    rewardSpot.getCoursePlaceCategory(),
                    rewardSpot.getThumbnailUrl(),
                    rewardSpot.getExpertComment(),
                    rewardSpot.getGeoJsonPoint().getX(),
                    rewardSpot.getGeoJsonPoint().getY()
            );
        }
    }

    public record RewardCourseDeleteDto(
            @JsonProperty("isDeleted") boolean isDeleted,
            String rewardCourseId,
            LocalDateTime deletedAt
    ){
        public static RewardCourseDeleteDto of(String rewardCourseId){
            return new RewardCourseDeleteDto(true,rewardCourseId,LocalDateTime.now());
        }
    }
}
