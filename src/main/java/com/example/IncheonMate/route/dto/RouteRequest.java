package com.example.IncheonMate.route.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class RouteRequest {

    public record RouteSearchRequest(
            @NotBlank(message = "출발지 X좌표(sx)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "출발지 X좌표는 숫자 형식이어야 합니다.")
            String sx,

            @NotBlank(message = "출발지 Y좌표(sy)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "출발지 Y좌표는 숫자 형식이어야 합니다.")
            String sy,

            @NotBlank(message = "도착지 X좌표(ex)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "도착지 X좌표는 숫자 형식이어야 합니다.")
            String ex,

            @NotBlank(message = "도착지 Y좌표(ey)는 필수입니다.")
            @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "도착지 Y좌표는 숫자 형식이어야 합니다.")
            String ey,

            @NotBlank(message = "출발지 명칭은 필수입니다.")
            String departureName,

            @NotBlank(message = "도착지 명칭은 필수입니다.")
            String arrivalName
    ){}
}
