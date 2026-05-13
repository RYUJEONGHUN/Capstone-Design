package com.example.IncheonMate.reward.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.reward.dto.RewardRequest;
import com.example.IncheonMate.reward.dto.RewardResponse;
import com.example.IncheonMate.reward.service.RewardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "리워드 코스 API", description = "여행 코스 탭 리워드 코스 화면에 필요한 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    //리워드 코스 목록의 간략한 정보
    @GetMapping
    public ResponseEntity<List<RewardResponse.RewardCourseSummaryDto>> getRewardCourses(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Reward] 리워드 코스 목록 조회 요청");

        List<RewardResponse.RewardCourseSummaryDto> result = rewardService.retrieveRewardCourses(user.getIdentifier());
        log.info("[Reward] 리워드 코스 목록 조회 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    //리워드 코스의 상세 정보
    @GetMapping("/{reward-course-id}")
    public ResponseEntity<RewardResponse.RewardCourseDto> getRewardCourseDetail(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable(name = "reward-course-id") String rewardCourseId){
        log.info("[Reward] [Detail] 리워드 코스 정보 조회 요청 (RewardCourseId: {})", rewardCourseId);

        RewardResponse.RewardCourseDto result = rewardService.retrieveRewardCourse(user.getIdentifier(), rewardCourseId);
        log.info("[Reward] [Detail] 리워드 코스 정보 조회 성공 (RewardCourseId: {})", rewardCourseId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    //QR코드를 인증하면 달성률 변경
    //가정: 우리 서비스 내에서 웹 카메라를 띄워 상점 방문을 확인한다고 가정
    @PostMapping("/{reward-course-id}/spots/{place-id}/verify")
    public ResponseEntity<RewardResponse.VerifySpotResponseDto> verifyVisit(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable(name = "reward-course-id") String rewardCourseId,
            @PathVariable(name = "place-id") String placeId,
            @RequestBody RewardRequest.VerifySpotRequest verifySpotRequest){

        log.info("[Reward] [Verify] 리워드 스팟 방문 인증 요청 (RewardCourseId : {}, PlaceId: {})",rewardCourseId,placeId);

        RewardResponse.VerifySpotResponseDto result = rewardService.validateVisit(user.getIdentifier(), rewardCourseId, placeId, verifySpotRequest.qrCodeUrl());
        log.info("[Reward] [Verify] 리워드 스팟 방문 인증 성공 (verifiedAt: {})", result.verifiedAt());

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }


    //리워드 코스 완료시 '내기프트' 내 선물함에 보상 지급
    //보상 받기 버튼을 누르면 달성률을 확인하고 100%일 때에만 지급하는 방식
    @PostMapping("/{reward-course-id}/reward")
    public ResponseEntity<RewardResponse.IssuedRewardResponseDto> verifyAndIssueCoupon(@AuthenticationPrincipal CustomOAuth2User user,@PathVariable(name = "reward-course-id") String rewardCourseId){
        log.info("[Reward] [Issue] 쿠폰 지급 요청 (RewardCourseId: {})", rewardCourseId);

        RewardResponse.IssuedRewardResponseDto result = rewardService.validateAndIssueCoupon(user.getIdentifier(), rewardCourseId);
        log.info("[Reward] [Issue] 달성률 100% 검증 및 쿠폰 발급 완료 (IssuedAt: {})", result.issuedAt());

        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

}
