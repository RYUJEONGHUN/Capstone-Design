package com.example.IncheonMate.member.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.dto.*;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.member.domain.type.SasangType;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final MemberCommonService memberCommonService;

    //현재 약관 버전
    private static final String CURRENT_TERMS_VERSION = "v1.0.0";

    //사상의학 테스트 결과 도출
    public MemberCommonDto.SasangResponseDto deriveSasangResult(List<MemberCommonDto.SasangAnswerDto> testResult, String email) {
        //체질 도출 로직
        SasangType sasangType = memberCommonService.analyzeSasangType(testResult);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        return new MemberCommonDto.SasangResponseDto(email,sasangType);
    }

    //초기 입력 화면(온보딩)에서 사용자가 입력한 정보 전체의 정책을 검사하고 저장하는 서비스
    @Transactional
    public void saveOnboarding(String email, OnboardingBundle.OnboardingDto onboardingDto) {
        /*
        String nickname -> 최소 2글자/'사용자' 미포함
        String birthdate -> 6자리 숫자
        String gender -> Enum,not NULL
        String mbti -> 대소문자 허용
        String profileImage; -> nullable
        CompanionType companion ->not null
        SasangType sasang -> not null
        String selectedPersonaId -> not null
        lang -> kor or eng
        */
        //온보딩DTO null 검증
        if (onboardingDto == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"저장할 온보딩 데이터가 업습니다.");
        }

        //저장할 멤버
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);

        //닉네임이 정책에 맞게 들어왔는지 검증
        if (!memberCommonService.checkNicknamePolicy(onboardingDto.nickname())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,onboardingDto.nickname() + "은(는) 정책을 위배한 닉네임입니다");
        }
        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = memberCommonService.parseLocalDate(onboardingDto.birthDate());

        //기존 멤버에 온보딩 DTO를 반영함
        //생년월일-현재보다 미래의 날짜도 통과하는 문제 => isAfter()로 해결
        //profileImageURL-null이면 exception나오는 문제 => 반드시 profileImageURL: null 형태로 전달받아야함(없으면 exception)
        Member updateMember = targetMember.toBuilder()
                .nickname(onboardingDto.nickname())
                .birthDate(birthDate)
                .gender(onboardingDto.gender())
                .mbti(memberCommonService.parseMbti(onboardingDto.mbti()))
                .profileImageURL(onboardingDto.profileImageURL())
                .profileImageAsMarker(StringUtils.hasText(onboardingDto.profileImageURL()))
                .companion(onboardingDto.companion())
                .sasang(onboardingDto.sasang())
                .selectedPersona(onboardingDto.selectedPersona())
                .lang(onboardingDto.lang())
                .build();
        //ROLE_GUEST -> ROLE_USER
        updateMember.upgradeRole();

        memberRepository.save(updateMember);
        log.info("'{}' 가입 완료",email);
    }



    //saveAgreements컨트롤러
    //약관 동의 내역을 검사하고 모두 동의 했을때에만 저장하는 서비스
    @Transactional
    public OnboardingBundle.TermsAgreementResponse saveAgreements(String email, OnboardingBundle.TermsAgreementRequest termsAgreementRequest) {
        //저장할 멤버
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
        //현재 시간
        LocalDateTime now = LocalDateTime.now();
        //약관 버전->버전관리 필요하면 메소드 형태로 변형
        String currentTermsVersion = CURRENT_TERMS_VERSION;

        //멤버 약관 동의 저장
        Member updatedMember =  targetMember.toBuilder()
                .isPrivacyPolicyAgreed(termsAgreementRequest.isPrivacyPolicyAgreed())
                .isLocationServiceAgreed(termsAgreementRequest.isLocationServiceAgreed())
                .isTermsOfServiceAgreed(termsAgreementRequest.isTermsOfServiceAgreed())
                .allTermsAgreedAt(now)
                .termsVersion(currentTermsVersion)
                .build();
        memberRepository.save(updatedMember);
        log.info("'{}' 약관 동의 내역 저장 완료",email);
        return OnboardingBundle.TermsAgreementResponse.from(updatedMember);
    }

    //getOnboardingData 서비스
    public OnboardingBundle.OnboardingDto getOnboardingValues(String email) {
        return OnboardingBundle.OnboardingDto.from(memberRepository.findByEmailOrElseThrow(email));
    }
}
