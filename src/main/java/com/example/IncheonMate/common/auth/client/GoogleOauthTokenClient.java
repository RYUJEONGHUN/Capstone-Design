package com.example.IncheonMate.common.auth.client;

import com.example.IncheonMate.common.auth.dto.GoogleOauthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "googleOauthTokenClient",
        url = "https://oauth2.googleapis.com"
)
public interface GoogleOauthTokenClient {

    @PostMapping(
            value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    GoogleOauthResponse.TokenResponse getGoogleToken(
            @RequestParam("code") String code,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("grant_type") String grantType
    );
}

