package com.example.IncheonMate.member.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.member.dto.OnboardingDto;
import com.example.IncheonMate.member.dto.SasangAnswerDto;
import com.example.IncheonMate.member.dto.TermsAgreementDto;
import com.example.IncheonMate.member.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    //약관 동의 저장
    //인자: HTTP body-약관1,2,3:ture
    //응답: HTTP body-이메일,동의한 시간,약관 버전
    @PostMapping("/agreements")
    public ResponseEntity<OnboardingService.AgreementResponse> saveAgreements(@AuthenticationPrincipal CustomOAuth2User user,
                                                            @RequestBody @Valid TermsAgreementDto termsAgreementDto){
        String email = user.getEmail();
        log.info("'{}' 약관 동의 내역 저장 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingService.saveAgreements(email,termsAgreementDto));
    }



    //사용자 정보 입력(온보딩) 시작
    //리턴: 입력받아야 할 값들을 key:value(null) 형태
    @GetMapping
    public ResponseEntity<OnboardingDto> getOnboardingData(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("'{}' 온보딩 시작 요청",user.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new OnboardingDto());
    }

    //닉네임 중복검사
    //인자: URI 파라미터-닉네임
    //리턴: true (사용불가/중복) or false (사용가능)
    @GetMapping("/check")
    public ResponseEntity<OnboardingService.NicknameAvailabilityResponse> checkNicknameAvailability(@RequestParam("nickname") String nickname,
                                                             @AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' 닉네임 중복 및 정책 검사 요청: {}",email,nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingService.isNicknameAvailability(email, nickname));
    }


    //사상의학 테스트 결과
    //인자: Http Body- 문항 번호(key): 선택한 답(value)
    //리턴: 체질 결과
    @PostMapping("/sasang/result")    //결과를 어떻게 받을지와 무엇을 넘겨줄지 아직 결정 안함
    public ResponseEntity<OnboardingService.SasangResultResponse> submitSasangTest(@RequestBody List<SasangAnswerDto> testResult,
                                                            @AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' 사상의학 테스트 결과 판별 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingService.deriveSasangResult(testResult,email));
    }


    //사용자가 입력한 정보 받아와서 검사하고 저장/올바르지 않은 정보이면 받았던 그대로 되돌려줘야함
    //인자: Http Body-사용자가 입력한 모든 온보딩 정보
    //리턴: 저장 성공-String || 저장 실패-OnboardingDto 그대로
    //저장: 온보딩 정보 member doc에 저장
    @PostMapping("/complete")
    public ResponseEntity<OnboardingDto> completeOnboarding(@RequestBody @Valid OnboardingDto onboardingDto,
                                                            @AuthenticationPrincipal CustomOAuth2User user) {
        String email = user.getEmail();
        log.info("'{}' 온보딩 데이터 검증 및 저장 요청", email);

        onboardingService.saveOnboarding(email, onboardingDto);
        log.info("'{}' 가입 완료",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingDto);
    }



}
