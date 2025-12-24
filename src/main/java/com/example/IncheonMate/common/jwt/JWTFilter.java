package com.example.IncheonMate.common.jwt;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.member.dto.MemberDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 헤더에서 Authorization 키 값 꺼내기
        String authorization = request.getHeader("Authorization");

        // 2. 토큰이 없거나 Bearer로 시작하지 않으면 통과 (로그인 안 한 상태)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 부분 자르고 순수 토큰만 획득
        String token = authorization.split(" ")[1];

        // 3. 토큰 만료 여부 확인
        if (jwtUtil.isExpired(token)) {
            log.info("토큰 만료");
            filterChain.doFilter(request, response);
            return;
        }

        // 4. 토큰에서 email, role 획득
        String email = jwtUtil.getEmail(token);
        String role = jwtUtil.getRole(token);

        // 5. MemberDTO 생성 (DB 조회 없이 토큰 정보로만 만듦 -> 빠름)
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail(email);
        memberDto.setRole(role);
        // 이름은 토큰에 없으니 임시로 설정 (필요하면 토큰에 넣어도 됨)
        memberDto.setName("User");

        // 6. UserDetails에 담아 Authentication 생성
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(memberDto);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());

        // 7. 세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}