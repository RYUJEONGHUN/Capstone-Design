package com.example.IncheonMate.reward.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.repository.PlaceRepository;
import com.example.IncheonMate.reward.domain.Reward;
import com.example.IncheonMate.reward.dto.AdminRewardRequest;
import com.example.IncheonMate.reward.dto.AdminRewardResponse;
import com.example.IncheonMate.reward.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AdminRewardService {

    private final RewardRepository rewardRepository;
    private final PlaceRepository placeRepository;

    //GET: /api/admin/rewards
    public List<AdminRewardResponse.RewardDto> retrieveRewards() {
        List<Reward> rewardList = rewardRepository.findAll();
        if (rewardList.isEmpty()) {
            log.info("[Reward] [Admin] 등록된 리워드/쿠폰 목록이 없음");
            return rewardList.stream()
                    .map(reward -> AdminRewardResponse.RewardDto.from(null))
                    .toList();
        }

        return rewardList.stream()
                .map(reward -> AdminRewardResponse.RewardDto.from(reward))
                .toList();
    }


    //GET: /api/admin/rewards/{naegift-id}
    public AdminRewardResponse.RewardDto retrieveRewardDetail(String naegiftId) {
        Optional<Reward> rewardOpt = rewardRepository.findByNaegiftId(naegiftId);
        if (!rewardOpt.isPresent()) {
            log.info("[Reward] [Admin] 등록된 리워드/쿠폰이 없음");
            return AdminRewardResponse.RewardDto.from(null);
        }

        return AdminRewardResponse.RewardDto.from(rewardOpt.get());
    }

    //DELETE: /api/admin/rewards/{naegift-id}/coupons/{coupon-id}
    @Transactional
    public AdminRewardResponse.CouponDeleteDto removeCouponFromReward(String naegiftId, String couponId) {
        Optional<Reward> rewardOpt = rewardRepository.findByNaegiftId(naegiftId);
        if (!rewardOpt.isPresent()) {
            log.info("[Reward] [Admin] 등록된 리워드/쿠폰이 없음");
            return null;
        }
        Reward targetReward = rewardOpt.get();

        Reward.Coupon targetCoupon = targetReward.getCoupons().stream()
                .filter(coupon -> coupon.getCouponId().equals(couponId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REWARD_CONDITION, "해당하는 쿠폰을 찾을 수 없습니다."));

        if (!targetReward.getCoupons().remove(targetCoupon)) {
            log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 실패 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "내기프트ID or 쿠폰ID를 잘못 입력하였습니다.");
        }

        targetReward.updateRemainStock();
        rewardRepository.save(targetReward);
        log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 성공 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);

        return AdminRewardResponse.CouponDeleteDto.of(couponId, true, LocalDateTime.now());
    }

    //POST: /api/admin/rewards
    public AdminRewardResponse.CouponRegistrationDto createReward(AdminRewardRequest.CouponInfoDto couponInfoDto) {

        String naegiftId = couponInfoDto.naegiftId();
        String couponId = couponInfoDto.couponId();

        Optional<Place> placeOpt = placeRepository.findByNaegiftId(naegiftId);
        if (!placeOpt.isPresent()) {
            log.warn("[Reward] [Admin] 내기프트ID에 해당하는 Place 없음");
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }

        Reward.Coupon targetCoupon = Reward.Coupon.builder()
                .couponId(couponId)
                .purchasedAt(LocalDateTime.now())
                .rewardCourseId(couponInfoDto.rewardCourseId())
                .isDelivered(false)
                .deliveredUserId(null)
                .deliveredAt(null)
                .expiredAt(LocalDate.now().plusMonths(couponInfoDto.expirationPeriodMonths()))
                .build();


        Reward targetReward = rewardRepository.findByNaegiftId(naegiftId)
                .orElseGet(() -> Reward.builder()
                        .placeId(placeOpt.get().getId())
                        .naegiftId(naegiftId)
                        .remainStock(1)
                        .isActive(true)
                        .coupons(new ArrayList<>())
                        .build()
                );

        boolean isDuplicateCoupon = targetReward.getCoupons().stream()
                .anyMatch(coupon -> coupon.getCouponId().equals(couponId));

        if (isDuplicateCoupon) {
            log.warn("[Reward] [Admin] [Create] 이미 존재하는 쿠폰 ID 등록 시도 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 쿠폰 ID입니다.");
        }

        targetReward.getCoupons().add(targetCoupon);
        targetReward.updateRemainStock();

        rewardRepository.save(targetReward);
        return AdminRewardResponse.CouponRegistrationDto.of(targetReward, targetCoupon);
    }
}
