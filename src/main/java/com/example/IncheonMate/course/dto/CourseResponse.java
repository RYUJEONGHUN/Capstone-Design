package com.example.IncheonMate.course.dto;

import com.example.IncheonMate.chat.dto.ChatResponse;
import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;

import java.util.List;

public class CourseResponse {

    public record CourseSpotDto(
            int spotOrder, //여행 코스 순서
            String name, // 장소명
            String address, //주소
            String thumbnailUrl, //사진
            CoursePlaceCategory coursePlaceCategory, //카테고리(
            String kakaoUrl,
            String naegiftUrl, //내기프트 URL
            String expertComment,
            Double x,
            Double y
    ) {
    }

    public record TravelCourseDto(
            String courseId,
            String title,
            List<ChatResponse.CourseSpotDto> courseSpots
    ) {
    }

    public record TravelCourseSummaryDto(
            String courseId,
            String title,
            String thumbnailUrl,
            List<CoursePlaceCategory> coursePlaceCategories
    ){}

}
