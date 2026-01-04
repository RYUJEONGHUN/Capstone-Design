package com.example.IncheonMate.common.auth.handler;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate; // Redis 안 쓰면 주석 처리
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate; // 무조건 사용 (Nullable 아님)

    // application.yml에서 주소 가져오기 (하드코딩 제거)
    @Value("${app.frontend.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 유저 정보 추출
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String email = customUserDetails.getEmail();
        String role = customUserDetails.getAuthorities().iterator().next().getAuthority();

        // 2. 토큰 생성
        long accessTime = 60 * 60 * 1000L; // 1시간
        long refreshTime = 14 * 24 * 60 * 60 * 1000L; // 14일

        String accessToken = jwtUtil.createJwt(email, role, accessTime);
        String refreshToken = jwtUtil.createJwt(email, role, refreshTime);

        // 3. Refresh Token -> Redis 저장
        redisTemplate.opsForValue()
                .set("RT:" + email, refreshToken, 14, TimeUnit.DAYS);
        log.info("Refresh Token Redis 저장 완료: {}", email);

        // 4. Refresh Token -> HttpOnly 쿠키로 굽기
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)            // 자바스크립트 접근 불가 (XSS 방지)
                .secure(true)             // 로컬(http) 환경에서는 false여야 함! (배포 시 true로 변경)
                .path("/")                 // 모든 경로에서 쿠키 전송
                .maxAge(Duration.ofDays(14))
                .sameSite("None")          // ✅ cross-site 쿠키 필수
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // 5. Access Token -> URL로 전달 (프론트 편의성 타협)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}