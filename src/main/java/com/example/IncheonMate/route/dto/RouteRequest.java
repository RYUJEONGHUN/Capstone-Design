package com.example.IncheonMate.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RouteRequest {

    public record RouteSearchRequest(
            @Schema(description = "출발지 X좌표 (경도)", example = "126.978388")
            @NotBlank(message = "출발지 X좌표(sx)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "출발지 X좌표는 숫자 형식이어야 합니다.")
            String sx,

            @Schema(description = "출발지 Y좌표 (위도)", example = "37.566610")
            @NotBlank(message = "출발지 Y좌표(sy)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "출발지 Y좌표는 숫자 형식이어야 합니다.")
            String sy,

            @Schema(description = "도착지 X좌표 (경도)", example = "127.027619")
            @NotBlank(message = "도착지 X좌표(ex)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "도착지 X좌표는 숫자 형식이어야 합니다.")
            String ex,

            @Schema(description = "도착지 Y좌표 (위도)", example = "37.497942")
            @NotBlank(message = "도착지 Y좌표(ey)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "도착지 Y좌표는 숫자 형식이어야 합니다.")
            String ey,

            @Schema(description = "출발지 명칭", example = "서울시청")
            @NotBlank(message = "출발지 명칭은 필수입니다.")
            String departureName,

            @Schema(description = "도착지 명칭", example = "강남역")
            @NotBlank(message = "도착지 명칭은 필수입니다.")
            String arrivalName
    ){}
}
