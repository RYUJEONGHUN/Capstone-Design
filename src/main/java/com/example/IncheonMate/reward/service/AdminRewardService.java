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

    private final RewardRepository rewardRepositoryrepository;
    private final PlaceRepository placeRepository;

    //GET: /api/admin/rewards
    public List<AdminRewardResponse.RewardDto> retrieveRewards() {
        List<Reward> rewardList = rewardRepositoryrepository.findAll();
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
        Optional<Reward> rewardOpt = rewardRepositoryrepository.findByNaegiftId(naegiftId);
        if (!rewardOpt.isPresent()) {
            log.info("[Reward] [Admin] 등록된 리워드/쿠폰이 없음");
            return AdminRewardResponse.RewardDto.from(null);
        }

        return AdminRewardResponse.RewardDto.from(rewardOpt.get());
    }

    //DELETE: /api/admin/rewards/{naegift-id}/coupons/{coupon-id}
    @Transactional
    public AdminRewardResponse.CouponDeleteDto removeCouponFromReward(String naegiftId, String couponId) {
        Optional<Reward> rewardOpt = rewardRepositoryrepository.findByNaegiftId(naegiftId);
        if (!rewardOpt.isPresent()) {
            log.info("[Reward] [Admin] 등록된 리워드/쿠폰이 없음");
            return null;
        }
        Reward targetReward = rewardOpt.get();

        Reward.Coupon targetCoupon = targetReward.getCoupons().stream()
                .filter(coupon -> coupon.getId().equals(couponId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REWARD_CONDITION, "해당하는 쿠폰을 찾을 수 업습니다."));

        if (!targetCoupon.getCouponId().equals(couponId) && !targetReward.getNaegiftId().equals(naegiftId)) {
            log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 실패 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "내기프트ID or 쿠폰ID를 잘못 입력하였습니다.");
        }
        if (!targetReward.getCoupons().remove(targetCoupon)) {
            log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 실패 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "내기프트ID or 쿠폰ID를 잘못 입력하였습니다.");
        }

        rewardRepositoryrepository.save(targetReward);
        log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 성공 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);

        return AdminRewardResponse.CouponDeleteDto.of(couponId, true, LocalDateTime.now());
    }

    //POST: /api/admin/rewards
    public AdminRewardResponse.CouponRegistrationDto createReward(AdminRewardRequest.CouponInfoDto couponInfoDto) {

        String naegiftId = couponInfoDto.naegiftId();
        String couponId = couponInfoDto.couponId();
        Optional<Place> placeOpt = placeRepository.findByNaegiftId(naegiftId);
        if (!placeOpt.isPresent()) {
            log.warn("[Rward] [Admin] 내기프트ID에 해당하는 Place 없음");
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        Reward.Coupon targetCoupon = Reward.Coupon.builder()
                .couponId(couponId)
                .purchasedAt(LocalDateTime.now())
                .rewardCourseId(couponInfoDto.rewaradCourseId())
                .isDelivered(false)
                .deliveredUserId(null)
                .deliveredAt(null)
                .expiredAt(LocalDate.now().plusMonths(couponInfoDto.expirationPeriodMonths()))
                .build();

        //1. 내기프트ID를 포함하는 reward collection이 있는지 확인하고 없으면 생성
        Reward targetReward = rewardRepositoryrepository.findByNaegiftId(naegiftId)
                .orElseGet(() -> {
                    List<Reward.Coupon> initialCoupons = new ArrayList<>();
                    initialCoupons.add(targetCoupon);

                    return Reward.builder()
                            .placeId(placeOpt.get().getId())
                            .naegiftId(naegiftId)
                            .remainStock(1)
                            .isActive(true)
                            .coupons(initialCoupons)
                            .build();
                });

        //2. 내기프트ID를 포함하는 reward collection에 coupon 넣고 쿠폰 재고 1증가시키기
        targetReward.getCoupons().add(targetCoupon);
        targetReward.increaseRemainStock();

        rewardRepositoryrepository.save(targetReward);
        return AdminRewardResponse.CouponRegistrationDto.of(targetReward,targetCoupon);
    }
}
