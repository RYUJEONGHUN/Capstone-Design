package com.example.IncheonMate.common.auth.service;

import com.example.IncheonMate.common.auth.client.KakaoOauthTokenClient;
import com.example.IncheonMate.common.auth.client.KakaoOauthUserInfoClient;
import com.example.IncheonMate.common.auth.dto.KakaoOauthResponse;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.common.jwt.JWTUtil;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoSdkOauthService {

    //컨트롤러로 토큰 전송하기 위한 dto
    public record Tokens(String accessToken, String refreshToken, String role){}

    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
    private final KakaoOauthTokenClient kakaoOauthTokenClient;
    private final KakaoOauthUserInfoClient kakaoOauthUserInfoClient;

    @Value("${KAKAO_CLIENT_ID}")
    private String clientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Transactional
    public Tokens kakaoLogin(String code) {
        //1. 코드로 token 받아오기
        KakaoOauthResponse.TokenResponse tokenResponse = kakaoOauthTokenClient.getTokens(
                "authorization_code",
                clientId,
                redirectUri,
                code,
                clientSecret);
        log.debug("카카오 토큰 받기 성공-Access Token 만료 {}초 남음", tokenResponse.expiresIn());

        //2. token으로 유저 정보 받아오기
        KakaoOauthResponse.UserInfoResponse userInfoResponse = kakaoOauthUserInfoClient.getInfo("Bearer " + tokenResponse.accessToken());
        log.debug("카카오에서 유저 정보 받기 성공-성공 시간: {}", userInfoResponse.connectedAt());

        //3. 회원가입 또는 로그인 처리
        String email = userInfoResponse.kakaoAccount().email();
        String name = null;
        //프로필이 null이 아니고 닉네임이 있을때만 name 추출
        if(userInfoResponse.kakaoAccount().profile() != null){
            name = userInfoResponse.kakaoAccount().profile().nickname();
        }
        //유효성 검사
        if(!StringUtils.hasText(email) || !StringUtils.hasText(name)){
            log.error("필수 정보 누락: email={}, nickname={}", email, name);
            throw new CustomException(ErrorCode.MISSING_REQUIRED_INFO);
        }

        //3.2 멤버 생성 or 불러오기
        String finalName = name;
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("신규 회원 가입 진행: {}", email);
                    Member newMember = Member.builder()
                            .email(email)
                            .name(finalName)
                            .role("ROLE_GUEST")
                            .provider("kakao")
                            .build();
                    return memberRepository.save(newMember);
                });

        //4. 우리 서비스의 JWT 발급
        long accessTime = 60 * 60 * 1000L; // 1시간
        long refreshTime = 14 * 24 * 60 * 60 * 1000L; // 14일

        String accessToken = jwtUtil.createJwt(email, member.getRole(), accessTime);
        String refreshToken = jwtUtil.createJwt(email, member.getRole(), refreshTime);

        //5. Refresh Token을 redis에 저장
        redisTemplate.opsForValue()
                .set("RT:" + email, refreshToken, 14, TimeUnit.DAYS);
        log.info("Refresh Token Redis 저장 완료: {}", email);

        return new Tokens(accessToken,refreshToken,member.getRole());
    }
}
