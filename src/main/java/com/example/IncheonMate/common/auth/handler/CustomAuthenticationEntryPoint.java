package com.example.IncheonMate.common.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        //1. JWTFilter에서 예외가 발생했을 때 request에 저장해둔 속성 값을 가져온다
        String exceptionAttribute = (String) request.getAttribute("exception");
        String requestUri = request.getRequestURI();

        //2. 로그 기록(디버깅용)
        log.warn("[JWT 인증 실패] URI: {}, Error: {}",requestUri,authException.getMessage());

        //3. 응답 헤더 설정(JSON,UTF-8)
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        //4. 응답 바디 구성
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("path", requestUri);

        //5. 상황별 에러 코드 및 메시지 분기 처리
        if("TOKEN_EXPIRED".equals(exceptionAttribute)){
            //CASE A:토큰 만료 -> 프론트엔드가 토큰 재발급 요청(/auth/refresh) 시도
            body.put("code", "TOKEN_EXPIRED");
            body.put("message", "Access token이 만료되었습니다. 재발급이 필요합니다.");
        } else{
            //CASE B: 토큰 없음, 위조됨, 그 외의 모든 인증 실패 -> 프론트엔드가 로그인 페이지로 이동
            body.put("code", "LOGIN_REQUIRED");
            body.put("message","인증이 필요합니다. 로그인 해주세요");
        }

        //6. JSON으로 변환하여 응답 전송
        objectMapper.writeValue(response.getWriter(),body);
    }
}
