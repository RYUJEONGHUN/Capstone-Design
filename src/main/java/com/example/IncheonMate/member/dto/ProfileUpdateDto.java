package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.CompanionType;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileUpdateDto {
    //Member Profile 속성 업데이트용 Dto
    String nickname;
    MbtiType mbti;
    SasangType sasang;
    CompanionType companion;
    LocalDate birthDate;
}
