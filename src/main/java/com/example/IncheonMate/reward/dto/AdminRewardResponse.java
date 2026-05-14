package com.example.IncheonMate.reward.dto;

import com.example.IncheonMate.reward.domain.Reward;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminRewardResponse {

    // 등록 성공 시 응답 (CouponRegisterDto에서 이름을 좀 더 명확하게 변경 가능)
    public record CouponRegistrationDto(
            String naegiftId,
            int remainStock,
            @JsonProperty("isActive") boolean isActive,
            String couponId,
            String rewardCourseId,
            LocalDateTime purchasedAt,
            LocalDate expiredAt // exexpiredAt -> expiredAt 오타 수정
    ){
        public static CouponRegistrationDto of(Reward reward, Reward.Coupon coupon){
            return new CouponRegistrationDto(
                    reward.getNaegiftId(),
                    reward.getRemainStock(),
                    reward.isActive(),
                    coupon.getCouponId(),
                    coupon.getRewardCourseId(),
                    coupon.getPurchasedAt(),
                    coupon.getExpiredAt()
            );
        }
    }

    // 목록/상세 조회용
    public record RewardDto(
            String naegiftId,
            String placeId,
            List<CouponDto> coupons
    ) {
        public static RewardDto from(Reward reward){
            if(reward == null) return null;

            return new RewardDto(
                    reward.getNaegiftId(),
                    reward.getPlaceId(),
                    reward.getCoupons().stream()
                            .map(coupon -> CouponDto.from(coupon))
                            .toList()
            );
        }
    }

    // 쿠폰 정보 표현
    public record CouponDto(
            String couponId,
            LocalDateTime purchasedAt,
            LocalDate expiredAt
    ){
        public static CouponDto from(Reward.Coupon coupon){
            return new CouponDto(
                    coupon.getCouponId(),
                    coupon.getPurchasedAt(),
                    coupon.getExpiredAt()
            );
        }
    }

    // 삭제 성공 결과 확인용
    public record CouponDeleteDto(
            String couponId,
            @JsonProperty("isDeleted") boolean isDeleted,
            LocalDateTime deletedAt
    ){
        public static CouponDeleteDto of(String couponId, boolean isDeleted, LocalDateTime deletedAt){
            return new CouponDeleteDto(couponId,isDeleted,deletedAt);
        }
    }
}
