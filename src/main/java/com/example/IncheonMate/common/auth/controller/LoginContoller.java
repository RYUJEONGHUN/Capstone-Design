package com.example.IncheonMate.common.auth.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.auth.dto.LoginDto;
import com.example.IncheonMate.common.auth.dto.Tokens;
import com.example.IncheonMate.common.auth.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginContoller {

    private final LoginService loginService;

    //@Value("${app.frontend.redirect-url}")
    //private String redirectUrl;

    @Operation(summary = "카카오/구글 로그인", description = "카카오/구글 SDK 로그인 로직을 수동으로 진행하여 상황에 맞는 토큰을 생성하여 전송 및 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "가입을 하지 않은 사용자 상세 정보 입력을 위한 토큰 발급"),
            @ApiResponse(responseCode = "200", description = "가입을 이미 완료한 사용자를 위한 토큰 발급")
    })
    @PostMapping("/user/login")
    public ResponseEntity<?> socialLogin(@RequestBody LoginDto.UserRequest userRequest, HttpServletResponse response,@AuthenticationPrincipal CustomOAuth2User user) {
        Tokens tokens = loginService.processSocialLogin(userRequest,user);

        //1. 신규 가입자
        if("ROLE_PENDING".equals(tokens.role())) {
            //1.1 게스트 출신 신규 가입자(user가 null이 아님)
            if(user != null){
                LoginDto.GuestProfile guestProfile = loginService.getProfileInRedis(user.getIdentifier());
                log.info("게스트 계정 있는 사용자 소셜 로그인 요청 성공");
                return ResponseEntity.status(HttpStatus.OK)
                        .body(LoginDto.Response.from(tokens,guestProfile));
            }

            // 1.2 게스트 계정이 없는 신규 가입자(nser가 null임)
            log.info("게스트 계정이 없는 사용자 소셜 로그인 요청 성공");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(LoginDto.Response.onlyToken(tokens));
        }


        //2. 이미 가입완료한 사용자-access/refresh token 모두 발급
        //refresh token 쿠키에 등록
        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        //accessToken과 role을 return
        log.info("이미 가입 완료한 사용자 로그인 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(LoginDto.Response.onlyToken(tokens));

    }

    @Operation(summary = "게스트 로그인", description = "게스트로 로그인을 진행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accees Token 전송 및 Refresh Token,게스트 정보 저장 완료")
    })
    @PostMapping("/guest/login")
    public ResponseEntity<LoginDto.Response> guestLogin(@RequestBody LoginDto.GuestRequest guestRequest, HttpServletResponse response) {
        log.info("신규 게스트 로그인 요청");

        LoginDto.GuestLoginResult result  = loginService.guestLogin(guestRequest);

        //1. Refresh Token을 HttpOnly Cookie에 굽는다
        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", result.tokens().refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        //2. Access Token과 Role을 Http Body에 담아서 전송한다.
        log.info("신규 게스트 계정 생성 완료");
        return ResponseEntity.status(HttpStatus.OK)
                .body(LoginDto.Response.from(result.tokens(), result.guestProfile()));
    }

}
