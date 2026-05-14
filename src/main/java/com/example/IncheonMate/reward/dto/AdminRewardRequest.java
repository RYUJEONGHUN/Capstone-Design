package com.example.IncheonMate.reward.dto;

public class AdminRewardRequest {

    public record CouponInfoDto(
            String naegiftId,
            String couponId,
            String rewaradCourseId,
            int expirationPeriodMonths
    ){}
}
