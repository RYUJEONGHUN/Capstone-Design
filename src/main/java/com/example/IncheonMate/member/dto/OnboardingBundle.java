package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.CompanionType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//초기 정보 입력(온보딩)에 필요한 DTO들을 모아놓은 클래스
public class OnboardingBundle {

    //약관 동의 결과 응답 DTO
    public record TermsAgreementResponse(
            String email,
            LocalDateTime agreedAt,
            String version) {
        public static TermsAgreementResponse from(Member member) {
            return new TermsAgreementResponse(
                    member.getEmail(),
                    member.getAllTermsAgreedAt(),
                    member.getTermsVersion());
        }
    }


    //약관 동의 내역 요청 DTO
    public record TermsAgreementRequest(
            @JsonProperty("isPrivacyPolicyAgreed")
            @AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다.")
            boolean isPrivacyPolicyAgreed,//개인정보 처리방침 동의

            @JsonProperty("isLocationServiceAgreed")
            @AssertTrue(message = "위치기반 서비스 이용약관에 동의해야 합니다.")
            boolean isLocationServiceAgreed, //위치기반 서비스 동의

            @JsonProperty("isTermsOfServiceAgreed")
            @AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다.")
            boolean isTermsOfServiceAgreed //개인정보 동의
    ) {
    }
    /*
    정상 흐름 json
    {
      "isPrivacyPolicyAgreed": true,
      "isLocationServiceAgreed": true,
      "isTermsOfServiceAgreed": true
    }

    비정상 흐름 json
    {
      "isPrivacyPolicyAgreed": true,
      "isLocationServiceAgreed": false,
      "isTermsOfServiceAgreed": true
    }
     */

    //온보딩에서 입력해야 하는 요청,응답 DTO
    public record OnboardingDto(
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
            String nickname,

            @NotBlank(message = "생년월일은 필수입니다.")
            @Pattern(
                    regexp = "^\\d{6}$",
                    message = "생년월일은 6자리 숫자여야 합니다. (예: 990101)"
            )
            //service에서 Stirng to LocalDateTIme 필수
            String birthDate,

            @NotBlank(message = "MBTI는 필수입니다.")
            @Pattern(
                    regexp = "^(?i)[EI][NS][FT][PJ]$",
                    message = "올바른 MBTI 형식이 아닙니다. (예: ENFJ)"
            )
            //service에서 String to MbtiType 필수
            String mbti,

            @Nullable
            String profileImageURL,

            @NotNull(message = "동반자는 필수입니다.")
            CompanionType companion,

            @NotNull(message = "사상의학 테스트는 필수입니다.")
            SasangType sasang,

            @NotBlank(message = "페르소나를 선택해주세요")
            @NotNull
            String selectedPersonaId,

            @NotBlank(message = "언어는 필수입니다.")
            @Pattern(regexp = "^(kor|eng)$", message = "지원하지 않는 언어입니다. (kor 또는 eng만 가능)")
            @NotNull(message = "언어는 필수입니다.: 현재 언어는 null입니다.")
            String lang
    ) {
        public static OnboardingDto from(Member member){
            if (member == null) {
                return new OnboardingDto(null,null,null,null,null,null,null,null);
            }
            return new OnboardingDto(member.getNickname(),
                    member.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd")),
                    member.getMbti().toString(),
                    member.getProfileImageURL(),
                    member.getCompanion(),
                    member.getSasang(),
                    member.getSelectedPersonaId(),
                    member.getLang()
            );
        }
    }

    /*테스트 JSON
    {
      "nickname": "인천여행자",
      "birthDate": "990101",
      "mbti": "ENFJ",
      "profileImageURL": "https://example.com/profiles/user1.png",
      "companion": "COUPLE",
      "sasang": "SOEUM",
      "selectedPersonaId": "persona_bear",
      "lang": "kor"
    }
 */
}


