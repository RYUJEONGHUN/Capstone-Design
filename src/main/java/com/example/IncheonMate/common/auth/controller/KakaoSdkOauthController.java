package com.example.IncheonMate.common.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class KakaoSdkOauthController {

    @Operation(summary = "카카오 SDK 로그인 토큰 발급", description = "카카오 SDK 로그인 로직을 수동으로 진행하여 AccessToken과 RefreshToken을 전송 및 저장합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accees Token 전송 및 Refresh Token 저장 완료")
    })
    @GetMapping("/kakao/callback")
    public void makeTokes(@RequestParam String code){
        //1. 카카오 토큰 받기
        //2. 받은 토큰으로 사용자 정보 가져오기
        //3. 회원가입 또는 로그인 처리
            //3.1 이미 초기 정보를 입력해서 닉네임이 있는 사용자이면 ROLE_USER로 토큰 발급
            //3.1 초기 정보를 입력하지 않아서 닉네임이 null인 사용자이면 ROLE_GUEST로 토큰 발급
        //4. 우리 서비스의 JWT 발급
    }
}
