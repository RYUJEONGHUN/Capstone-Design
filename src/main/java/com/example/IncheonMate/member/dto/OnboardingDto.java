package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.type.CompanionType;
import com.example.IncheonMate.member.type.MbtiType;
import com.example.IncheonMate.member.type.SasangType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OnboardingDto {
    private String nickname;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthDate;
    private MbtiType mbti;
    private String profileImage;
    private CompanionType companion;
    private SasangType sasang;
    private String selectedPersonaId;
}
