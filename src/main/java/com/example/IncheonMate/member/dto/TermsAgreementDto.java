package com.example.IncheonMate.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter // Getter만 필요하다면 이것만!
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
public class TermsAgreementDto {
    @JsonProperty("isPrivacyPolicyAgreed")
    @AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다.")
    private boolean isPrivacyPolicyAgreed; //개인정보 처리방침 동의

    @JsonProperty("isLocationServiceAgreed")
    @AssertTrue(message = "위치기반 서비스 이용약관에 동의해야 합니다.")
    private boolean isLocationServiceAgreed; //위치기반 서비스 동의

    @JsonProperty("isTermsOfServiceAgreed")
    @AssertTrue(message = "개인정보 수집 및 이용에 동의해야 합니다.")
    private boolean isTermsOfServiceAgreed; //개인정보 동의
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