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
                "https://4cd2403d53b0.ngrok-free.app",
                "http://localhost:3000"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));

        // (선택) 프론트에서 응답 헤더를 읽어야 할 때
        config.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        // (선택) 프리플라이트 캐시
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
