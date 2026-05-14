package com.example.IncheonMate.reward.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminRewardResponse {

    // 등록 성공 시 응답 (CouponRegisterDto에서 이름을 좀 더 명확하게 변경 가능)
    public record CouponRegistrationDto(
            String naegiftId,
            int remainStock,
            boolean isActive,
            String couponId,
            LocalDateTime purchasedAt,
            String rewardCourseId,
            LocalDate expiredAt // exexpiredAt -> expiredAt 오타 수정
    ){}

    // 목록/상세 조회용 (필드 정의 필요)
    public record RewardDto(
            String naegiftId,
            String title,
            List<CouponDto> coupons
    ) {}

    // 쿠폰 정보 표현
    public record CouponDto(
            String couponId,
            String status,
            LocalDate expiredAt
    ){}

    // 삭제 성공 결과 확인용
    public record CouponDeleteDto(
            String couponId,
            boolean isDeleted,
            LocalDateTime deletedAt
    ){}
}
