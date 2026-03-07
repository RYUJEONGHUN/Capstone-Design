package com.example.IncheonMate.common.auth.controller;

import com.example.IncheonMate.common.auth.service.KakaoSdkOauthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoSdkOauthController {

    private final KakaoSdkOauthService kakaoSdkOauthService;

    @Value("${app.frontend.redirect-url}")
    private String redirectUrl;

    @Operation(summary = "카카오 SDK 로그인 토큰 발급", description = "카카오 SDK 로그인 로직을 수동으로 진행하여 AccessToken과 RefreshToken을 전송 및 저장합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accees Token 전송 및 Refresh Token 저장 완료")
    })
    @GetMapping("/kakao/callback")
    public void makeTokes(@RequestParam String code, HttpServletResponse response) throws IOException {
        log.info("카카오 SDK 로그인 요청 code: {}", code);

        //로그인 과정은 service에서 하고 Access와 Refresh Token과 role만 가져옴
        KakaoSdkOauthService.Tokens tokens = kakaoSdkOauthService.kakaoLogin(code);

        //Access Token과 role은 전송하고 Refresh Token은 Cookie에 저장
        //Refresh Token Cookie에 저장
        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        //Access Token과 Role을 URI로 전달
        String targetUri = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("accessToken", tokens.accessToken())
                .queryParam("role", tokens.role())
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        log.info("프론트엔드로 리다이렉트-ROLE: {}", tokens.role());

        response.sendRedirect(targetUri);

    }
}
