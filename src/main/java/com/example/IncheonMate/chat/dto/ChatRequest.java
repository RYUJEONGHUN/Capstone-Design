package com.example.IncheonMate.chat.dto;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ChatRequest {

    @Schema(name = "ChatMessageRequest")
    public record MessageDto(
            @NotBlank String message
    ){}

    public record CourseSpotDto(
            int spotOrder, //여행 코스 순서
            String name, // 장소명
            String address, //주소
            String thumbnailUrl, //사진
            CoursePlaceCategory coursePlaceCategory, //카테고리(
            String kakaoUrl, //카카오 ID
            String naegiftUrl, //내기프트 URL
            String expertComment,
            Double x,
            Double y
    ){}

    public record TravelCourseDto(
            String title,
            List<CourseSpotDto> courseSpots
    ){}
}
