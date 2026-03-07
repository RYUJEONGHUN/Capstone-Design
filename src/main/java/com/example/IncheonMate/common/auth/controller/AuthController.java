package com.example.IncheonMate.common.auth.controller;

import com.example.IncheonMate.common.jwt.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "JWT Token Management", description = "Auth 2.0으로 인증한 유저의 Token 관리 기능")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Operation(summary = "Token 재발급 ", description = "Refresh Token을 확인하고 Refresh Token Rotation 방식을 사용하여 Access,Refresh Token을 재발급한다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refresh,Access Token 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token 인증 실패")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        // 1) 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractCookie(request, "refreshToken");
        if (!StringUtils.hasText(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "No refresh token"));
        }

        // 2) refreshToken 유효성 검사 (서명/만료)
        if (jwtUtil.isExpired(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "Refresh token expired"));
        }

        // 3) refreshToken에서 사용자 정보 추출
        String email = jwtUtil.getEmail(refreshToken);
        String role  = jwtUtil.getRole(refreshToken);

        // 4) Redis에 저장된 refreshToken과 비교
        String saved = redisTemplate.opsForValue().get("RT:" + email);
        if (!StringUtils.hasText(saved) || !saved.equals(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid refresh token"));
        }

        // 5) 새 토큰 발급
        long accessTimeMs = 60 * 60 * 1000L; // 1시간
        long refreshTimeMs = 14L * 24 * 60 * 60 * 1000L; // 14일

        String newAccess = jwtUtil.createJwt(email, role, accessTimeMs);

        //  refreshToken 회전: 새 refresh 발급 + Redis/쿠키 갱신
        String newRefresh = jwtUtil.createJwt(email, role, refreshTimeMs);
        redisTemplate.opsForValue().set("RT:" + email, newRefresh, 14, TimeUnit.DAYS);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefresh)
                .httpOnly(true)
                .secure(true)      // 로컬 http면 false, ngrok(https)면 true 권장
                .path("/")
                .maxAge(Duration.ofDays(14))
                .sameSite("None")    // ngrok/크로스사이트 상황이면 "None" + secure(true) 필요할 수 있음
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());
        log.info("새로운 Token 발급 완료: {}", email);

        // 6) 새 accessToken 전달 (JSON으로)
        return ResponseEntity.ok(Map.of(
                "accessToken", newAccess
        ));
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    @Operation(summary = "로그아웃", description = "Redis의 Refresh Token을 삭제하고 쿠키를 초기화하여 로그아웃합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response){
        log.info("로그아웃 요청");
        //1. 쿠키에서 refresh Token 추출
        String refreshToken = extractCookie(request, "refreshToken");

        //2. refresh token이 없으면 이미 로그아웃된 것으로 간주하여 200 반환
        if(!StringUtils.hasText(refreshToken)){
            log.debug("refresh token이 없어서 이미 로그아웃 되어있음");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(Map.of("message","이미 로그아웃 되었습니다."));
        }

        //3. 유효성 검사 및 Redis에서 삭제
        try{
            //유효성 검사-이메일을 꺼낼 수 없으면 유효하지 않음
            String email = jwtUtil.getEmail(refreshToken);
            //Redis에서 삭제
            redisTemplate.delete("RT: " + email);
        } catch (Exception e){
            //예외. 어떤 에러가 나더라도 로그아웃은 성공해야함
            log.warn("로그아웃 프로세스 중 토큰 처리 경고 (무시 가능): {}",e.getMessage());
        }

        //4. 쿠키에서 Refresh Token삭제(수명이 0인 쿠키를 넣어 즉시 만료)
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) //수명을 0으로 하여 즉시 만료시킴
                .sameSite("None")
                .build();

        //5. 응답 헤더에 삭제 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE,deleteCookie.toString());

        //6. 결과 반환
        log.info("로그아웃 성공");
        return ResponseEntity.ok(Map.of("message","로그아웃에 성공하였습니다"));
    }
}