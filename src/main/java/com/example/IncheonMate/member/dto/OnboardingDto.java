package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.CompanionType;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Unwrapped;

import java.time.LocalDate;

@Data
public class OnboardingDto {
    /*
    String nickname -> 최소 2글자,공백 허용/'사용자' 미포함
    String birthdate -> 6자리 숫자
    String mbti -> 대소문자 허용
    String profileImage; -> nullable
    CompanionType companion ->not null
    SasangType sasang -> not null
    String selectedPersonaId -> not blank,not null
     */

    @NotBlank(message = "닉네임은 필수입니다.")
    @Pattern(
            regexp = "^(?!.*사용자)[가-힣a-zA-Z0-9\\s]{2,10}$",
            message = "닉네임은 한글, 영문, 숫자, 공백을 포함한 2~10자여야 하며 '사용자'를 포함할 수 없습니다."
    )
    private String nickname;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "생년월일은 6자리 숫자여야 합니다. (예: 990101)"
    )
    //service에서 Stirng to LocalDateTIme 필수
    private String birthDate;

    @NotBlank(message = "MBTI는 필수입니다.")
    @Pattern(
            regexp = "^(?i)[EI][NS][FT][PJ]$",
            message = "올바른 MBTI 형식이 아닙니다. (예: ENFJ)"
    )
    //service에서 String to MbtiType 필수
    private String mbti;

    @Nullable
    private String profileImageURL;

    @NotNull(message = "동반자는 필수입니다.")
    private CompanionType companion;
    @NotNull(message = "사상의학 테스트는 필수입니다.")
    private SasangType sasang;
    @NotBlank(message = "페르소나를 선택해주세요")
    @NotNull
    private String selectedPersonaId;
}
/*테스트 JSON
{
  "nickname": "인천여행자",
  "birthDate": "990101",
  "mbti": "ENFJ",
  "profileImageURL": "https://example.com/profiles/user1.png",
  "companion": "COUPLE",
  "sasang": "SOEUM",
  "selectedPersonaId": "persona_bear"
}
 */