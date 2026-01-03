package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.SasangType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class MemberCommonDto {

    //닉네임 중복 체크
    public record NicknamePolicyDto(
            boolean isOk,
            String message
    ) {
        public static NicknamePolicyDto of(boolean isOk, String message) {
            return new NicknamePolicyDto(isOk,message);
        }
    }

    public record SasangRequestDto(
            @Valid
            @NotNull(message = "답안 리스트는 필수입니다.")
            @Size(min = 13, max = 13, message = "답안은 정확히 13개여야 합니다.") // 문항 수 강제 (선택사항)
            List<SasangAnswerDto> answers
    ){}

    public record SasangAnswerDto(
            @Min(value = 1, message = "문항 번호는 1 이상이어야 합니다.")
            int questionId,

            @Min(value = 1, message = "답변은 1 이상이어야 합니다.")
            @Max(value = 4, message = "답변은 4 이하여야 합니다.")
            int answer
    ){}

    public record SasangResponseDto(
            String email,
            SasangType sasangType
    ){}
}
