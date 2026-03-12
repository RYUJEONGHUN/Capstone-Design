package com.example.IncheonMate.common.auth.service;

import com.example.IncheonMate.common.auth.client.GoogleOauthTokenClient;
import com.example.IncheonMate.common.auth.client.GoogleOauthUserInfoClient;
import com.example.IncheonMate.common.auth.client.KakaoOauthTokenClient;
import com.example.IncheonMate.common.auth.client.KakaoOauthUserInfoClient;
import com.example.IncheonMate.common.auth.dto.*;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.common.jwt.JWTUtil;
import com.example.IncheonMate.member.domain.type.Role;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {


    private final JWTUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;
    private final KakaoOauthTokenClient kakaoOauthTokenClient;
    private final KakaoOauthUserInfoClient kakaoOauthUserInfoClient;
    private final GoogleOauthTokenClient googleOauthTokenClient;
    private final GoogleOauthUserInfoClient googleOauthUserInfoClient;

    @Value("${KAKAO_CLIENT_ID}")
    private String kakaoClientId;

    @Value("${KAKAO_CLIENT_SECRET}")
    private String kakaoClientSecret;

    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String googleClientSecret;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    public Tokens processSocialLogin(LoginDto.UserRequest userRequest, CustomOAuth2User user) {
    // 게스트 사용자가 소셜 인증을 완료한 경우에도 즉시 정회원으로 승격하지 않는다.
    // 추가 회원가입 절차를 진행하도록 ROLE_PENDING 토큰을 발급한다.

        String provider = userRequest.provider();
        String code = userRequest.code();
        String oAuthEmail = "";
        String oAuthName = "";

        try {
            if ("kakao".equals(provider)) {
                //1. code로 Access/Refresh Token 받아오기
                KakaoOauthResponse.TokenResponse kakaoOauthTokenResponse = kakaoOauthTokenClient.getKakaoTokens(
                        "authorization_code",
                        kakaoClientId,
                        kakaoRedirectUri,
                        code,
                        kakaoClientSecret
                );

                //2. Access token으로 유저 정보 받아오기
                KakaoOauthResponse.UserInfoResponse kakaoOauthUserResponse = kakaoOauthUserInfoClient.getKakaoInfo(
                        "Bearer " + kakaoOauthTokenResponse.accessToken());

                //3. 받아온 유저 정보에서 필요한 것만 추출하기
                oAuthEmail = kakaoOauthUserResponse.kakaoAccount().email();
                oAuthName = kakaoOauthUserResponse.kakaoAccount().profile().nickname();

            } else if ("google".equals(provider)) {
                //1. code로 Access/Refresh Token 받아오기
                GoogleOauthResponse.TokenResponse googleOauthTokenResponse = googleOauthTokenClient.getGoogleToken(
                        code,
                        googleClientId,
                        googleClientSecret,
                        googleRedirectUri,
                        "authorization_code"
                );

                //2. Access token으로 유저 정보 받아오기
                GoogleOauthResponse.UserInfoResponse googleOauthUserInfoResponse = googleOauthUserInfoClient.getGoogleInfo(
                        "Bearer " + googleOauthTokenResponse.accessToken());

                //3. 받아온 유저 정보에서 필요한 것만 추출하기
                oAuthEmail = googleOauthUserInfoResponse.email();
                oAuthName = googleOauthUserInfoResponse.name();

            } else {
                log.warn("지원하지 않는 소셜 로그인 제공자: {}", provider);
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 소셜 로그인 provider입니다.");
            }
        }catch (CustomException e){
            throw e;
        } catch (Exception e) {
            log.error("소셜 로그인 실패. provider={}", provider, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "소셜 로그인 중 문제 발생");
        }

        //이메일과 이름 검증
        if (oAuthEmail == null || oAuthEmail.isBlank()) {
            log.warn("{} 인증 서버에서 이메일 정보를 가져오지 못했습니다", provider);
            throw new CustomException(ErrorCode.INVALID_OAUTH_RESPONSE);
        }
        if (oAuthName == null || oAuthName.isBlank()) {
            log.warn("{} 인증 서버에서 이름 정보를 가져오지 못했습니다", provider);
            throw new CustomException(ErrorCode.INVALID_OAUTH_RESPONSE);
        }


        //3-분기1. 게스트로 가입한 적이 없거나 이미 가입한 사용자일 경우(ROLE_GUEST 토큰이 없을 때)
        if (user == null) {
            //4. DB에 email로 가입한 내역이 있는지 조회
            if (memberRepository.existsByEmail(oAuthEmail)) {
                //5-분기1. 이미 가입한 유저이기 때문에 User 토큰 발급(로그인 처리)
                String accessToken = jwtUtil.createJwt(oAuthEmail, Role.USER.getValue(), 60 * 60 * 1000L);
                String refreshToken = jwtUtil.createJwt(oAuthEmail, Role.USER.getValue(), 14 * 24 * 60 * 60 * 1000L);

                //6. refreshToken을 redis에 저장
                redisTemplate.opsForValue()
                        .set("RT:" + oAuthEmail, refreshToken, 14, TimeUnit.DAYS);
                log.info("Refresh Token Redis 저장 완료: {}", oAuthEmail);

                return Tokens.of(accessToken, refreshToken, Role.USER.getValue());
            } else {
                //5-분기2. 새롭게 가입해야하는 유저이기 때문에 Pending 토큰 발급
                //accessToken만, 만료기한 20분
                String accessToken = jwtUtil.createPendingJwt(oAuthEmail, "newUser", provider, Role.PENDING.getValue(), oAuthName, 20 * 60 * 1000L);

                return Tokens.of(accessToken, "", Role.PENDING.getValue());
            }
        }
        //3-분기2. 게스트로 가입한 적이 있는 사용자일 경우(ROLE_GUEST 토큰이 있을 때)
        else {
            //5-분기3. 게스트 계정이 있지만 새롭게 가입해야하는 유저이기 때문에 Pending 토큰 발급
            //accessToken만, 만료기한 20분/여기서 user.getEmail은 Redis의 UUID이다.
            String guestId = user.getIdentifier(); // 게스트 UUID
            String key = "GUEST_PROFILE:" + guestId;

            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                log.warn("만료된 게스트 계정으로 소셜 로그인 시도: {}", guestId);
                // 에러를 던져서 로그인 화면으로 보냄
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "게스트 계정 사용 기간이 만료되었습니다. 다시 로그인해 주세요.");

            }
            String accessToken = jwtUtil.createPendingJwt(oAuthEmail, user.getIdentifier(), provider, Role.PENDING.getValue(), oAuthName, 20 * 60 * 1000L);

            return Tokens.of(accessToken, "", Role.PENDING.getValue());
        }
    }


    public Tokens guestLogin(LoginDto.GuestRequest guestRequest) {
        //1. 게스트 UUID를 생성한다.
        String guestId = UUID.randomUUID().toString();

        //2. Redis에 GUEST_PROFILE:{UUID}로 게스트 정보를 저장한다/TTL은 14일이다
        Map<String, String> guestProfile = new HashMap<>();
        guestProfile.put("nickname", guestRequest.nickname());
        guestProfile.put("persona", guestRequest.personaType().toString());
        guestProfile.put("lang", guestRequest.lang());

        String key = "GUEST_PROFILE:" + guestId;
        redisTemplate.opsForHash().putAll(key, guestProfile);
        redisTemplate.expire(key, Duration.ofDays(14));

        //3. Access/Refresh Token을 만든다
        long accessTime = 60 * 60 * 1000L; // 1시간
        long refreshTime = 14 * 24 * 60 * 60 * 1000L; // 14일
        String accessToken = jwtUtil.createJwt(guestId, Role.GUEST.getValue(), accessTime);
        String refreshToken = jwtUtil.createJwt(guestId, Role.GUEST.getValue(), refreshTime);

        //4. Redis에 Refresh Token을 저장한다.
        redisTemplate.opsForValue()
                .set("RT:" + guestId, refreshToken, 14, TimeUnit.DAYS);

        //4. Access/Refresh Token과 Role을 Controller로 보낸다
        return Tokens.of(accessToken, refreshToken, Role.GUEST.getValue());
    }

    //redis에 저장되어 있는 게스트 정보 가져오기
    public Map<String, String> getProfileInRedis(String guestId) {
        String key = "GUEST_PROFILE:" + guestId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "게스트 정보를 찾을 수 없습니다.");
        }

        Map<String, String> guestProfile = new HashMap<>();
        guestProfile.put("nickname", (String) redisTemplate.opsForHash().get(key, "nickname"));
        guestProfile.put("persona", (String) redisTemplate.opsForHash().get(key, "persona"));
        guestProfile.put("lang", (String) redisTemplate.opsForHash().get(key, "lang"));

        return guestProfile;
    }

}
