package com.example.IncheonMate.reward.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Document(collection = "member_reward")
public class MemberReward {

    @Id
    private String id;
    @Indexed
    private String memberId;
    @Indexed
    private String rewardCourseId;

    //코스 완료 및 보상 상태 저장 필드
    @Builder.Default
    private boolean isCompleted = false;
    private LocalDateTime completedAt;
    @Builder.Default
    private boolean isRewarded = false;
    private LocalDateTime rewardedAt;

    @Builder.Default
    private List<RewardSpotProgress> spotProgressList = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RewardSpotProgress{
        private String placeId;
        @Builder.Default
        private boolean isVerified = false;
        private LocalDateTime verifiedAt;
    }
}
