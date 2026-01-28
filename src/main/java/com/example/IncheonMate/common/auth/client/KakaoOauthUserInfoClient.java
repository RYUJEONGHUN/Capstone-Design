package com.example.IncheonMate.common.auth.client;

import com.example.IncheonMate.common.auth.dto.KakaoOauthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "KakaoOauthUserInfoClient", url = "https://kapi.kakao.com")
public interface KakaoOauthUserInfoClient {

    //token을 전송해서 카카오로부터 email과 user_name받기
    @GetMapping(
            value = "/v2/user/me",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    KakaoOauthResponse.UserInfoResponse getInfo(
            @RequestHeader("Authorization") String accessToken //헤더
    );
}
