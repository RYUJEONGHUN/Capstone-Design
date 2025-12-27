package com.example.IncheonMate.common.security;

import com.example.IncheonMate.common.auth.handler.OAuth2SuccessHandler;
import com.example.IncheonMate.common.auth.service.CustomOAuth2UserService;
import com.example.IncheonMate.common.jwt.JWTFilter;
import com.example.IncheonMate.common.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CSRF 해제 (JWT 쓰면 필요 없음)
        http.csrf((auth) -> auth.disable());

        // 2. Form 로그인 방식 해제 (우리는 소셜로그인/JWT 쓸 거니까)
        http.formLogin((auth) -> auth.disable());
        http.httpBasic((auth) -> auth.disable());

        // 3. 경로별 인가 작업
        http.authorizeHttpRequests((auth) -> auth
                // 로그인, 메인, 헬스체크는 누구나 접근 가능
                .requestMatchers("/", "/login/**", "/oauth2/**", "/api/health").permitAll()
                // 스웨거 문서도 열어둠
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/swagger-ui.html").permitAll()
                // 나머지는 로그인한 사람만
                .anyRequest().authenticated());

        http.oauth2Login(oauth -> oauth
                .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
        );

        // 4. JWTFilter 등록 (기존 로그인 필터 앞에 끼워넣기)
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);


        // 5. 세션 설정 (Stateless: 서버에 세션 안 만듦)
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}