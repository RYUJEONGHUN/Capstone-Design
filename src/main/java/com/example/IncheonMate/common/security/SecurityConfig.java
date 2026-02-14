package com.example.IncheonMate.common.security;

import com.example.IncheonMate.common.auth.handler.CustomAuthenticationEntryPoint;
import com.example.IncheonMate.common.auth.handler.OAuth2SuccessHandler;
import com.example.IncheonMate.common.auth.service.CustomOAuth2UserService;
import com.example.IncheonMate.common.jwt.JWTFilter;
import com.example.IncheonMate.common.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CSRF 해제 (JWT 쓰면 필요 없음)
        http.csrf((auth) -> auth.disable());

        // 1.5. Cors 설정
        //http.cors(cors -> {});
        http.cors(cors -> cors.configurationSource(corsConfigurationSource));

        // 2. Form 로그인 방식 해제 (우리는 소셜로그인/JWT 쓸 거니까)
        http.formLogin((auth) -> auth.disable());
        http.httpBasic((auth) -> auth.disable());

        // 3. 경로별 인가 작업
        http.authorizeHttpRequests((auth) -> auth
                //Options(PreFlight)요청 모두 허용
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                // 로그인, 메인, 헬스체크는 누구나 접근 가능
                //26-01-25 /error 엔드포인트 추가: Spring 내부 에러를 401로 둔갑하는것 방지
                .requestMatchers("/login/**", "/oauth2/**", "/auth/refresh","/error","/auth/kakao/callback").permitAll()
                // 스웨거 문서도 열어둠
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**","/swagger-ui.html").permitAll()
                .requestMatchers("/auth/logout").permitAll()
                // 나머지는 로그인한 사람만
                .anyRequest().authenticated());

        http.oauth2Login(oauth -> oauth
                // Spring Security가 가로챌 엔드포인트를 다른 곳으로 지정 (예: /login/oauth2/code/*)
                // 이렇게 하면 /auth/kakao/callback은 더 이상 가로채지 않습니다.
                .redirectionEndpoint(redirection -> redirection
                        .baseUri("/login/oauth2/code/google")
                )
                .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
                .failureHandler((request, response, exception) -> {
                    // URL에 특수문자 [ ] 가 포함되지 않도록 인코딩 처리를 해주면 Tomcat 에러를 방지할 수 있습니다.
                    String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
                    response.sendRedirect("https://2025-capstone-design-fe.vercel.app/login?fail=true&message=" + errorMessage);
                })
        );


        // 4. JWTFilter 등록 (기존 로그인 필터 앞에 끼워넣기)
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);


        // 5. 세션 설정 (Stateless: 서버에 세션 안 만듦)
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring()
                .requestMatchers("/favicon.ico");
    }
}