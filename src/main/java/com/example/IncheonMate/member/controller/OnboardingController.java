package com.example.IncheonMate.member.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.auth.dto.LoginDto;
import com.example.IncheonMate.common.auth.dto.Tokens;
import com.example.IncheonMate.common.exception.ErrorResponse;
import com.example.IncheonMate.common.jwt.JWTUtil;
import com.example.IncheonMate.member.domain.type.Role;
import com.example.IncheonMate.member.dto.*;
import com.example.IncheonMate.member.service.MemberCommonService;
import com.example.IncheonMate.member.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
@Tag(name = "Member Onboarding API", description = "사용자 초기 개인정보 입력(온보딩) 기능")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final MemberCommonService memberCommonService;
    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    //약관 동의 저장
    //인자: HTTP body-약관1,2,3:ture
    //응답: HTTP body-이메일,동의한 시간,약관 버전
    @Operation(summary = "약관 동의 내역 확인 및 저장", description = "모든 약관에 동의했는지 확인하고 모두 동의 했으면 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "약관 동의 내역 저장 성공", content = @Content(schema = @Schema(implementation = OnboardingBundle.TermsAgreementResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/agreements")
    public ResponseEntity<OnboardingBundle.TermsAgreementResponse> saveAgreements(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user,
                                                                                  @Parameter(description = "약관 동의 여부(ture/false)") @RequestBody @Valid OnboardingBundle.TermsAgreementRequest termsAgreementRequest) {
        String email = user.getIdentifier();
        log.info("'{}' 약관 동의 내역 저장 요청", email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingService.saveAgreements(email, termsAgreementRequest));
    }


    //온보딩에서 입력한 값들 보여주기
    //리턴: 입력받아야 할 값들을 key:value 형태
    @Operation(summary = "초기 입력화면에서 저장한 정보 전체 제공", description = "온보딩 화면에서 사용자가 입력한 정보들을 모두 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "온보딩에서 입력한 정보 전체", content = @Content(schema = @Schema(implementation = OnboardingBundle.OnboardingDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<OnboardingBundle.OnboardingDto> getOnboardingData(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        log.info("'{}' 온보딩에서 저장한 정보 조회 요청", user.getIdentifier());

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingService.getOnboardingValues(user.getIdentifier()));
    }

    //닉네임 중복검사
    //인자: URI 파라미터-닉네임
    //리턴: true (사용불가/중복) or false (사용가능)
    @Operation(summary = "닉네임 중복 및 정책 검사", description = "사용자가 입력한 닉네임의 중복 검사와 정책 위반을 검사합니다.")
    @ApiResponse(responseCode = "200", description = "닉네임 검사 성공(ture or false 응답)", content = @Content(schema = @Schema(implementation = MemberCommonDto.NicknamePolicyDto.class)))
    @GetMapping("/check")
    public ResponseEntity<MemberCommonDto.NicknamePolicyDto> checkNicknameAvailability(@Parameter(description = "검사할 닉네임", example = "사용할 닉네임123") @RequestParam("nickname") String nickname,
                                                                                       @Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        String email = (user != null) ? user.getIdentifier() : null;
        log.info("닉네임 검사 요청 (가입유무: {}): {}", (email != null), nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberCommonService.isNicknameAvailability(email, nickname));
    }


    //사상의학 테스트 결과
    //인자: Http Body- 문항 번호(key): 선택한 답(value)
    //리턴: 체질 결과
    @Operation(summary = "사상의학 설문 결과 도출", description = "사상의학 설문에 대한 체질을 도출합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사상의학 결과 도출 성공", content = @Content(schema = @Schema(implementation = MemberCommonDto.SasangResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/sasang/result")
    public ResponseEntity<MemberCommonDto.SasangResponseDto> submitSasangTest(@RequestBody @Valid MemberCommonDto.SasangRequestDto testResult,
                                                                              @AuthenticationPrincipal CustomOAuth2User user) {
        String email = user.getIdentifier();
        log.info("'{}' 사상의학 테스트 결과 판별 요청", email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(onboardingService.deriveSasangResult(testResult.answers(), email));
    }


    //사용자가 입력한 정보 받아와서 검사하고 저장/올바르지 않은 정보이면 받았던 그대로 되돌려줘야함
    //인자: Http Body-사용자가 입력한 모든 온보딩 정보
    //저장: 온보딩 정보 member doc에 저장
    @Operation(summary = "온보딩 데이터 저장 및 토큰 재발급", description = "사용자가 입력한 정보를 저장하고 토큰을 재발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "온보딩 데이터 저장 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/complete")
    public ResponseEntity<LoginDto.Response> completeOnboarding(@RequestBody @Valid OnboardingBundle.OnboardingDto onboardingDto,
                                                                 @AuthenticationPrincipal CustomOAuth2User user,
                                                                 HttpServletRequest request, //JWT을 직접 꺼내어 파싱(/complete때 딱 한번)
                                                                 HttpServletResponse response) {
        String email = user.getIdentifier();
        log.info("'{}' 온보딩 데이터 검증 및 저장 요청", email);

        //토큰에서 {guestId,provider,name} 직접 꺼내옴
        String token = request.getHeader("Authorization").substring(7);
        String guestId = jwtUtil.getGuestId(token);
        String provider = jwtUtil.getProvider(token);
        String name = jwtUtil.getName(token);

        //1. DB에 정보 저장 및 ROLE을 PENDING -> USER로 변경
        onboardingService.saveOnboarding(email, onboardingDto,guestId,provider,name);

        //2. ROLE_USER 권한으로 새로운 토큰 발급
        long accessTime = 60 * 60 * 1000L;
        long refreshTime = 14 * 24 * 60 * 60 * 1000L;

        String newAccessToken = jwtUtil.createJwt(email,Role.USER.getValue(), accessTime);
        String newRefreshToken = jwtUtil.createJwt(email,Role.USER.getValue(), refreshTime);

        redisTemplate.opsForValue()
                .set("RT:" + email, newRefreshToken, 14, TimeUnit.DAYS);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE,cookie.toString());

        return ResponseEntity.status(HttpStatus.OK)
                .body(LoginDto.Response.success(
                        Tokens.of(newAccessToken,newRefreshToken,Role.USER.getValue())));
    }
}
