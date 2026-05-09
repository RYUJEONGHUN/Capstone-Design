package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.SasangType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

//온보딩과 MyInfo화면에 공통으로 사용하는 DTO들을 모아놓은 클래스
public class MemberCommonDto {

    //닉네임 중복 체크에 사용
    @Schema(description = "닉네임 중복 체크 응답")
    public record NicknamePolicyDto(
            @Schema(description = "사용 가능 여부", example = "true")
            boolean isOk,
            @Schema(description = "성공,실패 이유", example = "닉네임 중복: xxxx")
            String message
    ) {
        public static NicknamePolicyDto of(boolean isOk, String message) {
            return new NicknamePolicyDto(isOk,message);
        }
    }

    //사용자가 답한 선택지를 받아올 때 사용-이 DTO가 없으면 @Valid를 사용할 수 없음
    @Schema(description = "사상의학 설문 선택한 번호 목록")
    public record SasangRequestDto(
            @Valid
            @NotNull(message = "답안 리스트는 필수입니다.")
            @Size(min = 13, max = 13, message = "답안은 정확히 13개여야 합니다.") // 문항 수 강제 (선택사항)
            @ArraySchema(schema = @Schema(description = "선택한 번호 목록", implementation = SasangAnswerDto.class))
            List<SasangAnswerDto> answers
    ){}

    //사용자가 답한 선택지를 받아올 때 사용
    @Schema(description = "사상의학 설문 단일 문항")
    public record SasangAnswerDto(
            @Min(value = 1, message = "문항 번호는 1 이상이어야 합니다.")
            @Schema(description = "질문 번호", example = "1")
            int questionId,

            @Min(value = 1, message = "답변은 1 이상이어야 합니다.")
            @Max(value = 4, message = "답변은 4 이하여야 합니다.")
            @Schema(description = "선택한 답변 번호", example = "2")
            int answer
    ){}

    //사용자의 사상 체질 결과를 응답할 떄 사용
    @Schema(description = "사상의학 결과")
    public record SasangResponseDto(
            @Schema(description = "사용자 식별용 이메일", example = "test123@gamil.com")
            String email,
            @Schema(description = "사상의학 설문 결과", example = "SOEUM", implementation = SasangType.class)
            SasangType sasangType
    ){}
}
