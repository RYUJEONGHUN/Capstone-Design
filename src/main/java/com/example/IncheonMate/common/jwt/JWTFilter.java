package com.example.IncheonMate.common.jwt;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.member.dto.MemberDto;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate; //Guest 만료 확인을 위해 주입

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        // 1. 헤더에서 Authorization 키 값 꺼내기
        String authorization = request.getHeader("Authorization");

        // 2. 토큰이 없거나 Bearer로 시작하지 않으면 통과 (로그인 안 한 상태)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 부분 자르고 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        //모든 로직을 try-catch안에 넣어야 jwt exception 피할 수 있음
        //이전 로직은 토큰이 만료되는 경우는 검사하지 않고 email과 role을 꺼내서 jwt exception이 났음
        try {
            //1. 만료 여부 먼저 확인
            if (jwtUtil.isExpired(token)) {
                log.warn("[Auth] JWT Filter - 토큰 만료 (URI: {})", requestUri);
                throw new ExpiredJwtException(null, null, "만료된 토큰");
            }

            //2. 토큰이 유효할 때만 정보 추출
            String identifier = jwtUtil.getIdentifier(token);
            String role = jwtUtil.getRole(token);

            MDC.put("userId", identifier);
            log.debug("[Auth] JWT Filter - 토큰 검증 성공 (Identifier: {}, Role: {}, URI: {})", identifier, role, requestUri);

            if("ROLE_GUEST".equals(role)){
                if(Boolean.FALSE.equals(redisTemplate.hasKey("GUEST_PROFILE:"+ identifier))){
                    log.warn("[Auth] JWT Filter - 게스트 정보 Redis에 없음 (Identifier: {}, URI: {})", identifier, requestUri);
                    request.setAttribute("exception","GUEST_NOT_EXIST");
                    throw new IllegalStateException("[Auth] 게스트 정보 없음");
                }
            }

            //3. Member Dto 생성
            MemberDto memberDto = new MemberDto();
            memberDto.setIdentifier(identifier);
            memberDto.setRole(role);
            memberDto.setName("User");

            //4. 인증 객체 생성 및 저장
            // 4.1 UserDetails에 담아 Authentication 생성
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(memberDto);
            Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
            // 4.2 세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch(ExpiredJwtException e){
            log.warn("[Auth] JWT Filter - 토큰 만료 예외 (URI: {})", requestUri);
            request.setAttribute("exception", "TOKEN_EXPIRED");
        }catch (Exception e){
            log.warn("[Auth] JWT Filter - 유효하지 않은 토큰 예외 (URI: {}, Error: {})", requestUri, e.getMessage());
            if(request.getAttribute("exception") == null) {
                request.setAttribute("exception", "INVALID_TOKEN");
            }
        }

        filterChain.doFilter(request, response);
    }
}