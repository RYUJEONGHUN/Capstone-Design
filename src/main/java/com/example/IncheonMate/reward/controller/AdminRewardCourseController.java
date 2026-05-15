package com.example.IncheonMate.reward.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.reward.dto.AdminRewardCourseRequest;
import com.example.IncheonMate.reward.dto.AdminRewardCourseResponse;
import com.example.IncheonMate.reward.service.AdminRewardCourseService;
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
@RequestMapping("/api/admin/reward-courses")
public class AdminRewardCourseController {

    private final AdminRewardCourseService adminRewardCourseService;
    //리워드 코스 전체 조회
    @GetMapping
    public ResponseEntity<List<AdminRewardCourseResponse.RewardCourseDto>> findAllRewardCourses(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[RewardCourse] [Admin] 전체 리워드 코스 목록 조회 요청");

        List<AdminRewardCourseResponse.RewardCourseDto> result = adminRewardCourseService.retrieveRewardCourses();

        log.info("[RewardCourse] [Admin] 전체 리워드 코스 목록 조회 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }


    //리워드 코스 생성
    @PostMapping
    public ResponseEntity<AdminRewardCourseResponse.RewardCourseDto> createRewardCourse(@AuthenticationPrincipal CustomOAuth2User user, @RequestBody AdminRewardCourseRequest.RewardCourseCreateDto courseCreateDto){

        log.info("[RewardCourse] [Admin] 리워드 코스 생성 요청 (Title: {})", courseCreateDto.title()); // title 등 식별 가능한 필드 사용 권장

        AdminRewardCourseResponse.RewardCourseDto result = adminRewardCourseService.createRewardCourse(courseCreateDto);

        log.info("[RewardCourse] [Admin] 리워드 코스 생성 성공 (CourseId: {})", result.rewardCourseId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }


    //리워드 코스 삭제
    @DeleteMapping("/{reward-course-id}")
    public ResponseEntity<AdminRewardCourseResponse.RewardCourseDeleteDto> removeRewardCourse(@AuthenticationPrincipal CustomOAuth2User user,@PathVariable(name = "reward-course-id") String rewardCourseId){

        log.info("[RewardCourse] [Admin] [Remove] 리워드 코스 삭제 요청 (CourseId: {})", rewardCourseId);

        AdminRewardCourseResponse.RewardCourseDeleteDto result = adminRewardCourseService.removeRewardCourse(rewardCourseId);

        log.info("[RewardCourse] [Admin] [Remove] 리워드 코스 삭제 성공 (CourseId: {})", rewardCourseId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }
}
