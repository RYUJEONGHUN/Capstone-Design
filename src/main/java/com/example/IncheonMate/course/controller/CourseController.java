package com.example.IncheonMate.course.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.course.dto.CourseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행코스 API", description = "여행코스 화면에 필요한 API") //
@RestController
@Slf4j
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    //현재 코스(AI 생성 코스)의 상세 정보
    @GetMapping("/current")
    public ResponseEntity<CourseResponse.TravelCourseDto> getCurrentSelectedCourse(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Course] [Current] 현재 코스(AI 생성 코스) 상세 정보 조회 요청");
        return null;
    }

    //전체 코스 목록의 간략한 정보
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendedCourses(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Course] [Recommendation] 추천 코스 목록 조회 요청");
        return null;
    }

    //리워드 코스 목록의 간략한 정보
    @GetMapping("/rewards")
    public ResponseEntity<?> getRewardCourses(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Course] [Reward] 리워드 코스 목록 조회 요청");
        return null;
    }

    //추천 코스의 상세 정보
    @GetMapping("/recommendations/{course-id}")
    public ResponseEntity<?> getRecommendedCourseDetail(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable(name = "course-id") String courseId){
        log.info("[Course] [Recommendation] [Detail] 상세 코스 정보 조회 요청 (CourseId: {})", courseId);
        return null;
    }

    //리워드 코스의 상세 정보
    @GetMapping("/rewards/{course-id}")
    public ResponseEntity<?> getRewardCourseDetail(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable(name = "course-id") String rewardCourseId){
        log.info("[Course] [Reward] [Detail] 리워드 코스 정보 조회 요청 (RewardCourseId: {})", rewardCourseId);
        return null;
    }
}
