package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.SasangType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class MemberCommonDto {

    //닉네임 중복 체크
    public record NicknamePolicyDto(
            boolean isOk
    ) {
        public static NicknamePolicyDto from(boolean isOk) {
            return new NicknamePolicyDto(isOk);
        }
    }

    public record SasangAnswerDto(
            int questionId,
            @Min(1) @Max(4)
            int answer
    ){
    }

    public record SasangResultDto(
            String eamil,
            SasangType sasangType
    ){}
}
