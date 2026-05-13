package com.example.IncheonMate.reward.dto;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;

import java.time.LocalDateTime;
import java.util.List;

public class RewardResponse {

    //리워드 코스 요약 정보
    public record RewardCourseSummaryDto(
            String rewardCourseId,
            String title,
            String rewardDescription,
            int totalCount,
            int verifiedCount,
            boolean isCompleted,
            List<String> thumbnailUrls,
            List<CoursePlaceCategory> coursePlaceCategories
    ){}

    //리워드 코스 전체 정보
    public record RewardCourseDto(
        String rewardCourseId,
        String title,
        String rewardDescription,
        boolean isCompleted,
        boolean isRewarded,
        List<RewardCourseSpotDto> rewardCourseSpotDtos
        //int totalCount
        //int verifiedCount
    ){}

    //리워드 코스 스팟 상세 정보
    public record RewardCourseSpotDto(
            int spotOrder,
            String placeId,
            String kakaoUrl,
            String naegiftUrl,
            String name,
            String address,
            CoursePlaceCategory coursePlaceCategory,
            String thumbnailUrl,
            String expertComment,
            Double x,
            Double y,
            boolean isVerified,
            LocalDateTime verifiedAt
    ){}

    //방문 인증 응답
    public record VerifySpotResponseDto(
            String placeId,
            LocalDateTime verifiedAt,
            boolean isRewardCourseCompleted
            //int totalCount
            //int verifiedCount
    ){}

    //쿠폰(리워드) 발급 완료 응답
    //필드 모르겠음...
    public record IssuedRewardResponseDto(
        String rewardCourseId,
        String rewardTitle,
        String issuedAt
    ){}
}
