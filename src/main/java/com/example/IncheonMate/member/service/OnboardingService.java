package com.example.IncheonMate.member.service;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.dto.*;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.example.IncheonMate.persona.repository.PersonaRepository;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final PersonaRepository personaRepository;
    private final MemberCommonService memberCommonService;

    //현재 약관 버전
    private static final String CURRENT_TERMS_VERSION = "v1.0.0";

    //사상의학 테스트 결과 도출
    public MemberCommonDto.SasangResultDto deriveSasangResult(List<MemberCommonDto.SasangAnswerDto> testResult, String email) {
        //체질 도출 로직
        SasangType sasangType = memberCommonService.analyzeSasangType(testResult);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        return new MemberCommonDto.SasangResultDto(email,sasangType);
    }


    @Transactional
    public void saveOnboarding(String email, OnboardingBundle.OnboardingDto onboardingDto) {
        /*
        String nickname -> 최소 2글자/'사용자' 미포함
        String birthdate -> 6자리 숫자
        String mbti -> 대소문자 허용
        String profileImage; -> nullable
        CompanionType companion ->not null
        SasangType sasang -> not null
        String selectedPersonaId -> not blank,not null
        lang -> kor or eng
        */
        //온보딩DTO null 검증
        if (onboardingDto == null) {
            throw new IllegalArgumentException("온보딩 데이터가 없습니다.");
        }

        //저장할 멤버
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));

        //페르소나ID가 컬렉션에 있는것과 맞는지 검증
        String validatedPersonaId = validatePersonaId(onboardingDto.selectedPersonaId());
        //닉네임이 정책에 맞게 들어왔는지 검증
        if (!memberCommonService.checkNicknamePolicy(onboardingDto.nickname())) {
            throw new IllegalArgumentException("닉네임 정책에 맞지 않습니다: " + onboardingDto.nickname());
        }
        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = memberCommonService.parseLocalDate(onboardingDto.birthDate());

        //기존 멤버에 온보딩 DTO를 반영함
        //생년월일-현재보다 미래의 날짜도 통과하는 문제 => isAfter()로 해결
        //profileImageURL-null이면 exception나오는 문제 => 반드시 profileImageURL: null 형태로 전달받아야함(없으면 exception)
        //selectedPersonaId-컬렉션에 있는 personaId와 달라도 통과하는 문제 => 해결(vaildatePersoanId)
        Member updateMember = targetMember.toBuilder()
                .nickname(onboardingDto.nickname())
                .birthDate(birthDate)
                .mbti(parseMbti(onboardingDto.mbti()))
                .profileImageURL(onboardingDto.profileImageURL())
                .profileImageAsMarker(StringUtils.hasText(onboardingDto.profileImageURL()))
                .companion(onboardingDto.companion())
                .sasang(onboardingDto.sasang())
                .selectedPersonaId(validatedPersonaId)
                .lang(onboardingDto.lang())
                .build();

        memberRepository.save(updateMember);
    }

    private String validatePersonaId(String selectedPersonaId) {
        if (!personaRepository.existsById(selectedPersonaId)) {
            log.error("({})에 해당하는 페르소나ID가 없습니다.", selectedPersonaId);
            throw new NoSuchElementException("(" + selectedPersonaId + ")에 해당하는 페르소나ID가 없습니다.");
        }
        return selectedPersonaId;
    }

    private MbtiType parseMbti(String mbti) {
        return MbtiType.valueOf(mbti.toUpperCase());
    }


    //saveAgreements컨트롤러
    @Transactional
    public OnboardingBundle.TermsAgreementResponse saveAgreements(String email, OnboardingBundle.TermsAgreementRequest termsAgreementRequest) {
        //저장할 멤버
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
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

}
