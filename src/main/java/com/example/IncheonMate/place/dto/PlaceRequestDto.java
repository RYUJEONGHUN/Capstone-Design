package com.example.IncheonMate.place.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PlaceRequestDto {
    @NotBlank(message = "카카오 장소 ID는 필수입니다.")
    private String kakaoId;

    @NotNull(message = "별점은 필수입니다.")
    private Double ourRating;

    private String expertComment; // 큐레이션 멘트 (선택)

    private String thumbnailUrl;  // 대표 사진 URL (선택)

    private List<String> tags;    // 태그 목록
}
