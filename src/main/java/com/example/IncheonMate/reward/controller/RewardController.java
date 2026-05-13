package com.example.IncheonMate.reward.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리워드 코스 API", description = "여행 코스 탭 리워드 코스 화면에 필요한 API")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardController {

    //리워드 코스 목록의 간략한 정보
    @GetMapping
    public ResponseEntity<?> getRewardCourses(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Reward] 리워드 코스 목록 조회 요청");
        return null;
    }

    //리워드 코스의 상세 정보
    @GetMapping("/{reward-course-id}")
    public ResponseEntity<?> getRewardCourseDetail(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable(name = "reward-course-id") String rewardCourseId){
        log.info("[Reward] [Detail] 리워드 코스 정보 조회 요청 (RewardCourseId: {})", rewardCourseId);
        return null;
    }

    //QR코드를 인증하면 달성률 변경
    //가정: 우리 서비스 내에서 웹 카메라를 띄워 상점 방문을 확인한다고 가정
    @PostMapping("/{reward-course-id}/spots/{place-id}/verify")
    public ResponseEntity<?> verifyVisit(@AuthenticationPrincipal CustomOAuth2User user,@PathVariable(name = "reward-course-id") String rewardCourseId,  @PathVariable(name = "place-id") String placeId){
        log.info("[Reward] [Verify] 리워트 코스 인증 요청 (RewardCourseId : {}, PlaceId: {})",rewardCourseId,placeId);
        return null;
    }


    //리워드 코스 완료시 '내기프트' 내 선물함에 보상 지급
    //보상 받기 버튼을 누르면 달성률을 확인하고 100%일 때에만 지급하는 방식
    @PostMapping("/{reward-course-id}/reward")
    public ResponseEntity<?> verifyAndIssueCoupon(@AuthenticationPrincipal CustomOAuth2User user,@PathVariable(name = "reward-course-id") String rewardCourseId){
        log.info("[Reward] [Issue] 쿠폰 지급 요청 (RewardCourseId: {})", rewardCourseId);
        return null;
    }

}
