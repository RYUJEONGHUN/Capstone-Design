package com.example.IncheonMate.reward.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AdminRewardCourseRequest {

    public record RewardCourseCreateDto(
            String title,
            @JsonProperty("isVisible") boolean isVisible,
            String rewardDescription,
            List<String> naegiftIds
    ){}
}
