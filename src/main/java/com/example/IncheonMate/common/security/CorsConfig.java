package com.example.IncheonMate.common.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ 쿠키(RefreshToken) 주고받을 거면 필수
        config.setAllowCredentials(true);

        // ✅ ngrok 프론트 도메인 정확히(https 포함) 넣기
        // 예: https://abcd-1234.ngrok-free.app
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://*.ngrok-free.app",
                "https://triggerless-battlesome-teodoro.ngrok-free.dev",
                "https://unconducing-ungovernmental-hilaria.ngrok-free.dev",
                "https://2025-capstone-design-fe.vercel.app"
        ));

        // 🔴 [필수] 이 부분 주석을 반드시 풀어주세요! 🔴
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // ✅ 헤더는 * (모두 허용)로 설정하셨으므로 통과!
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "ngrok-skip-browser-warning" // <--- 이 친구가 핵심입니다!
        ));

        // (선택) 프론트에서 응답 헤더를 읽어야 할 때
        config.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        // (선택) 프론트에서 응답 헤더를 읽어야 할 때
        config.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        // (선택) 프리플라이트 캐시
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
