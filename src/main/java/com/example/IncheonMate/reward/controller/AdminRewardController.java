package com.example.IncheonMate.reward.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.reward.dto.AdminRewardRequest;
import com.example.IncheonMate.reward.dto.AdminRewardResponse;
import com.example.IncheonMate.reward.service.AdminRewardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "리워드 코스 관리자 API", description = "여행 코스 탭 리워드 코스 관리자에게 필요한 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/admin/rewards")
public class AdminRewardController {

    private final AdminRewardService adminRewardService;

    @PostMapping
    public ResponseEntity<AdminRewardResponse.CouponRegistrationDto> createReward(@AuthenticationPrincipal CustomOAuth2User user, @RequestBody AdminRewardRequest.CouponInfoDto couponInfoDto){
        log.info("[Reward] [Admin] 리워드 등록 요청 (NaegiftId: {})", couponInfoDto.naegiftId());

        AdminRewardResponse.CouponRegistrationDto result = adminRewardService.createReward(couponInfoDto);
        log.info("[Reward] [Admin] 리워드 등록 성공 (NaegiftId: {})", couponInfoDto.naegiftId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<AdminRewardResponse.RewardDto>> findAllRewards(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Reward] [Admin] 전체 리워드 목록 조회 요청");

        List<AdminRewardResponse.RewardDto> result = adminRewardService.retrieveRewards();
        log.info("[Reward] [Admin] 전체 리워드 목록 조회 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    @GetMapping("/{naegift-id}")
    public ResponseEntity<AdminRewardResponse.RewardDto> findRewardByNaegiftId(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable(name = "naegift-id") String naegiftId){
        log.info("[Reward] [Admin] 상세 조회 (NaegiftId: {})", naegiftId);

        AdminRewardResponse.RewardDto result = adminRewardService.retrieveRewardDetail(naegiftId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    @DeleteMapping("/{naegift-id}/coupons/{coupon-id}")
    public ResponseEntity<AdminRewardResponse.CouponDeleteDto> removeCouponFromReward(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable(name = "naegift-id") String naegiftId, @PathVariable(name = "coupon-id") String couponId){
        log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 요청 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);

        AdminRewardResponse.CouponDeleteDto result = adminRewardService.removeCouponFromReward(naegiftId,couponId);
        log.info("[Reward] [Admin] [Remove] 쿠폰 삭제 성공 (NaegiftId: {}, CouponId: {})", naegiftId, couponId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

}
