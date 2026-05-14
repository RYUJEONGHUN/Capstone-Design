package com.example.IncheonMate.reward.service;

import com.example.IncheonMate.reward.dto.AdminRewardCourseRequest;
import com.example.IncheonMate.reward.dto.AdminRewardCourseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AdminRewardCourseService {

    public List<AdminRewardCourseResponse.RewardCourseDto> retrieveRewardCourses() {
        return null;
    }

    public AdminRewardCourseResponse.RewardCourseDto createRewardCourse(AdminRewardCourseRequest.RewardCourseCreateDto courseCreateDto) {
        return null;
    }

    public AdminRewardCourseResponse.RewardCourseDeleteDto removeRewardCourse(String rewardCourseId) {
        return null;
    }
}
