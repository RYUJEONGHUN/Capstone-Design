package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.type.CompanionType;
import com.example.IncheonMate.member.type.MbtiType;
import com.example.IncheonMate.member.type.SasangType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProfileUpdateDto {
    //Member Profile 속성 업데이트용 Dto
    String nickname;
    MbtiType mbti;
    SasangType sasang;
    CompanionType companion;
    LocalDate birthDate;
}
