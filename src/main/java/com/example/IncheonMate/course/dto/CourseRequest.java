package com.example.IncheonMate.course.dto;

import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CourseRequest {

    public record CreateCuratedCourse(
            @NotBlank String title,
            @NotNull boolean isVisible,
            @NotNull
            @Size(min = 5, max = 5, message = "추천 코스는 반드시 1,2,3,4,5의 5개의 장소로 구성되어야 합니다.")
            List<CreateCuratedSpot> createCuratedSpotList
    ) {
    }

    public record CreateCuratedSpot(
            int spotOrder,
            @NotNull String placeId
    ) {
    }
}

