package com.example.IncheonMate.common.auth.client;

import com.example.IncheonMate.common.auth.dto.GoogleOauthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "googleOauthUserInfoClient",
        url = "https://openidconnect.googleapis.com"
)
public interface GoogleOauthUserInfoClient {

    @GetMapping("/v1/userinfo")
    GoogleOauthResponse.UserInfoResponse getGoogleInfo(
            @RequestHeader("Authorization") String authorization
    );
}
