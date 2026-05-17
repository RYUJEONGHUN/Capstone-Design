package com.example.IncheonMate.reward.dto;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.example.IncheonMate.reward.domain.MemberReward;
import com.example.IncheonMate.reward.domain.RewardCourse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RewardResponse {

    //리워드 코스 요약 정보
    //썸네일 이미지를 보여주기 위해서 /images 중간에 /thumbnail을 끼워넣음(임시)
    // /images/thumbnail/xxxxx.jpg
    // /images/xxxx.jpg
    public record RewardCourseSummaryDto(
            String rewardCourseId,
            String title,
            String rewardDescription,
            int totalCount,
            int verifiedCount,
            @JsonProperty("isCompleted") boolean isCompleted,
            List<String> thumbnailUrls,
            List<CoursePlaceCategory> coursePlaceCategories
    ) {
        public static RewardResponse.RewardCourseSummaryDto of(RewardCourse rewardCourse, MemberReward memberReward) {
            List<String> convertedThumbnails = rewardCourse.getRewardSpots().stream()
                    .map(RewardCourse.RewardSpot::getThumbnailUrl)
                    .map(url -> {
                        if (url != null && url.contains("/images/")) {
                            return url.replace("/images/", "/images/thumbnail/");
                        }
                        return url; // URL이 null이거나 패턴이 맞지 않으면 원본 반환
                    })
                    .toList();

            return new RewardCourseSummaryDto(
                    rewardCourse.getId(),
                    rewardCourse.getTitle(),
                    rewardCourse.getRewardDescription(),
                    rewardCourse.getRewardSpots().size(),
                    (int) memberReward.getSpotProgressList().stream().filter(MemberReward.RewardSpotProgress::isVerified).count(),
                    memberReward.isCompleted(),
                    convertedThumbnails,
                    rewardCourse.getRewardSpots().stream().map(RewardCourse.RewardSpot::getCoursePlaceCategory).toList()
            );
        }
    }

    //리워드 코스 전체 정보
    public record RewardCourseDto(
            String rewardCourseId,
            String title,
            String rewardDescription,
            @JsonProperty("isCompleted") boolean isCompleted,
            @JsonProperty("isRewarded") boolean isRewarded,
            int totalCount,
            int verifiedCount,
            List<RewardCourseSpotDto> rewardCourseSpotDtos

    ) {
        public static RewardCourseDto of(RewardCourse rewardCourse, MemberReward memberReward, Map<String, MemberReward.RewardSpotProgress> progressMap) {
            return new RewardCourseDto(
                    rewardCourse.getId(),
                    rewardCourse.getTitle(),
                    rewardCourse.getRewardDescription(),
                    memberReward.isCompleted(),
                    memberReward.isRewarded(),
                    rewardCourse.getRewardSpots().size(),
                    (int) memberReward.getSpotProgressList().stream().filter(MemberReward.RewardSpotProgress::isVerified).count(),
                    rewardCourse.getRewardSpots().stream()
                            .map(spot -> {
                                MemberReward.RewardSpotProgress progress = progressMap.get(spot.getPlaceId());
                                return RewardCourseSpotDto.of(spot, progress);
                            })
                            .toList());
        }
    }

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
            @JsonProperty("isVerified") boolean isVerified,
            LocalDateTime verifiedAt
    ) {
        public static RewardResponse.RewardCourseSpotDto of(RewardCourse.RewardSpot rewardSpot, MemberReward.RewardSpotProgress rewardSpotProgress) {
            return new RewardCourseSpotDto(
                    rewardSpot.getSpotOrder(),
                    rewardSpot.getPlaceId(),
                    "https://place.map.kakao.com/" + rewardSpot.getKakaoId(),
                    "https://shopuser-qa.naegift.com/" + rewardSpot.getNaegiftId() + "?channel_no=1",
                    rewardSpot.getName(),
                    rewardSpot.getAddress(),
                    rewardSpot.getCoursePlaceCategory(),
                    rewardSpot.getThumbnailUrl(),
                    rewardSpot.getExpertComment(),
                    rewardSpot.getGeoJsonPoint().getX(),
                    rewardSpot.getGeoJsonPoint().getY(),
                    rewardSpotProgress.isVerified(),
                    rewardSpotProgress.getVerifiedAt()
            );
        }
    }

    //방문 인증 응답
    public record VerifySpotResponseDto(
            String placeId,
            LocalDateTime verifiedAt,
            String redirectUrl,
            @JsonProperty("isCompleted") boolean isCompleted,
            LocalDateTime completedAt,
            int totalCount,
            int verifiedCount
    ) {
        public static VerifySpotResponseDto of(String placeId, String redirectUrl, MemberReward.RewardSpotProgress rewardSpotProgress, MemberReward memberReward) {
            return new VerifySpotResponseDto(
                    placeId,
                    rewardSpotProgress.getVerifiedAt(),
                    redirectUrl,
                    memberReward.isCompleted(),
                    memberReward.getCompletedAt(),
                    memberReward.getSpotProgressList().size(),
                    (int) memberReward.getSpotProgressList().stream().filter(MemberReward.RewardSpotProgress::isVerified).count()
            );
        }
    }

    //쿠폰(리워드) 발급 완료 응답
    public record IssuedRewardResponseDto(
            String rewardCourseId,
            String rewardTitle,
            LocalDateTime issuedAt,
            LocalDate expiredAt
    ) {
        public static IssuedRewardResponseDto of(String rewardCourseId, String rewardTitle, LocalDateTime issuedAt, LocalDate expiredAt) {
            return new IssuedRewardResponseDto(rewardCourseId, rewardTitle, issuedAt, expiredAt);
        }
    }
}
