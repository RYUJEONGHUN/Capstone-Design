package com.example.IncheonMate.common.auth.client;

import com.example.IncheonMate.common.auth.dto.KakaoOauthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="KakaoOauthTokenClient", url = "https://kauth.kakao.com")
public interface KakaoOauthTokenClient {


    //code를 전송해서 카카오oauth로 접근 가능한 accessToken과 refreshToken을 받기
    @PostMapping(
            value = "/oauth/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    KakaoOauthResponse.TokenResponse getTokens(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code,
            @RequestParam("client_secret") String clientSecret
    );
}
