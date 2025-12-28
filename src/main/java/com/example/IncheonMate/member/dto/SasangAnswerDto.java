package com.example.IncheonMate.member.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SasangAnswerDto {
    private int questionId;
    @Min(1) @Max(4)
    private int answer;
}
