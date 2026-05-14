package com.example.IncheonMate.common.auth.service;

import com.example.IncheonMate.common.auth.client.GoogleOauthUserInfoClient;
import com.example.IncheonMate.common.auth.client.KakaoOauthTokenClient;
import com.example.IncheonMate.common.auth.client.KakaoOauthUserInfoClient;
import com.example.IncheonMate.common.auth.dto.*;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.common.jwt.JWTUtil;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.PersonaType;
import com.example.IncheonMate.member.domain.type.Role;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${ADMIN_PASSWARD}")
    private String adminPassword;

    public Tokens processSocialLogin(LoginDto.UserRequest userRequest, CustomOAuth2User user) {
        // 게스트 사용자가 소셜 인증을 완료한 경우에도 즉시 정회원으로 승격하지 않는다.
        // 추가 회원가입 절차를 진행하도록 ROLE_PENDING 토큰을 발급한다.

        String provider = userRequest.provider();
        String code = userRequest.code();
        String oAuthEmail = "";
        String oAuthName = "";
        log.info("[Auth] 소셜 로그인 처리 시작 (Provider: {})", provider);

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
                //FeignClient는 기본적으로 JSON 형태로 변환하려고 하기 때문에 Form형식으로 변환하려면 의존성,config bean을 추가해야한다.
                //이 과정보다 Form 형식을 지원하는 RestTemplate를 쓰는게 더 좋다고 판단해 이 요청에만 RestTemplate 사용
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();

                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
                body.add("code", code);
                body.add("client_id", googleClientId);
                body.add("client_secret", googleClientSecret);
                body.add("redirect_uri", googleRedirectUri);
                body.add("grant_type", "authorization_code");

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

                // Google API 토큰 요청
                GoogleOauthResponse.TokenResponse googleOauthTokenResponse = restTemplate.postForObject(
                        "https://oauth2.googleapis.com/token",
                        request,
                        GoogleOauthResponse.TokenResponse.class);

                //2. Access token으로 유저 정보 받아오기
                GoogleOauthResponse.UserInfoResponse googleOauthUserInfoResponse = googleOauthUserInfoClient.getGoogleInfo(
                        "Bearer " + googleOauthTokenResponse.accessToken());

                //3. 받아온 유저 정보에서 필요한 것만 추출하기
                oAuthEmail = googleOauthUserInfoResponse.email();
                oAuthName = googleOauthUserInfoResponse.name();

            } else {
                log.warn("[Auth] 지원하지 않는 소셜 로그인 제공자: {}", provider);
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 소셜 로그인 provider입니다.");
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[Auth] 소셜 로그인 실패 (Provider: {})", provider, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "소셜 로그인 중 문제 발생");
        }

        //이메일과 이름 검증
        if (oAuthEmail == null || oAuthEmail.isBlank()) {
            log.warn("[Auth] {} 인증 서버에서 이메일 정보를 가져오지 못함", provider);
            throw new CustomException(ErrorCode.INVALID_OAUTH_RESPONSE);
        }
        if (oAuthName == null || oAuthName.isBlank()) {
            log.warn("[Auth] {} 인증 서버에서 이름 정보를 가져오지 못함", provider);
            throw new CustomException(ErrorCode.INVALID_OAUTH_RESPONSE);
        }
        //3. DB에 이메일이 있는지 부터 확인
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(oAuthEmail);

        //4-분기1. 이미 가입된 정회원인 경우
        if (existingMemberOpt.isPresent()) {
            // ==========================================
            // [분기 A] 이미 가입된 정회원인 경우
            // ==========================================
            Member existingMember = existingMemberOpt.get();

            //게스트 토큰을 가진 기존 회원 방어(기존 회원이 게스트 계정을 또 만들어서 가입하려고 하는것 방어)
            if (user != null && user.isGuest()) {
                log.info("[Auth] 기존 회원이 게스트 상태에서 로그인 시도 - 게스트 데이터 무시 (Email: {})", oAuthEmail);
            }

            // 소셜 정보(provider)가 다를 경우 로그만 남기기
            //+++++++++++ [계정 통합] 이메일이 동일하면 소셜 제공자(Provider)에 상관없이 기존 계정으로 로그인 처리 ++++++++++++++++
            if (!existingMember.getProvider().equals(provider)) {
                log.info("[Auth] 이메일 기반 계정 통합 로그인 (Email: {}, 기존 Provider: {}, 시도 Provider: {})",
                        oAuthEmail, existingMember.getProvider(), provider);
            } else {
                log.info("[Auth] 기존 계정 로그인 성공 (Email: {})", oAuthEmail);
            }

            //이미 가입한 유저이기 때문에 User 토큰 발급(로그인 처리)
            String accessToken = jwtUtil.createJwt(oAuthEmail, Role.USER.getValue(), 60 * 60 * 1000L);
            String refreshToken = jwtUtil.createJwt(oAuthEmail, Role.USER.getValue(), 14 * 24 * 60 * 60 * 1000L);

            //refreshToken을 redis에 저장
            redisTemplate.opsForValue()
                    .set("RT:" + oAuthEmail, refreshToken, 14, TimeUnit.DAYS);
            log.info("[Auth] Refresh Token Redis 저장 완료 (Email: {})", oAuthEmail);

            return Tokens.of(accessToken, refreshToken, Role.USER.getValue(), oAuthEmail);
        }
        //4-분기2. 가입하지 않은 진짜 신규 유저인 경우
        else {
            // ==========================================
            // [분기 B] DB에 없는 진짜 신규 유저인 경우
            // ==========================================
            if (user == null || !user.isGuest()) {
                // [분기 B-1] 쌩신규 유저 (게스트 이력 없음)
                log.info("[Auth] 신규 가입자 PENDING 토큰 발급 (Email: {}, Provider: {})", oAuthEmail, provider);
                String accessToken = jwtUtil.createPendingJwt(oAuthEmail, "newUser", provider, Role.PENDING.getValue(), oAuthName, 20 * 60 * 1000L);
                return Tokens.of(accessToken, "", Role.PENDING.getValue(),oAuthEmail);
            } else {
                // [분기 B-2] 게스트 출신 신규 유저
                String guestId = user.getIdentifier(); // 토큰에서 추출한 게스트 ID
                log.info("[Auth] 게스트 출신 신규 가입자 PENDING 토큰 발급 (Email: {}, Guest ID: {})", oAuthEmail, guestId);
                String accessToken = jwtUtil.createPendingJwt(oAuthEmail, guestId, provider, Role.PENDING.getValue(), oAuthName, 20 * 60 * 1000L);
                return Tokens.of(accessToken, "", Role.PENDING.getValue(),oAuthEmail);
            }

        }
    }


    public LoginDto.GuestLoginResult guestLogin(LoginDto.GuestRequest guestRequest) {
        //1. 게스트 UUID를 생성한다.
        String guestId = UUID.randomUUID().toString();
        log.info("[Auth] 게스트 로그인 처리 시작 (Guest ID: {}, Persona: {})", guestId, guestRequest.personaType());

        //2. Redis에 GUEST_PROFILE:{UUID}로 게스트 정보를 저장한다/TTL은 14일이다
        Map<String, String> guestProfileForSave = new HashMap<>();
        guestProfileForSave.put("persona", guestRequest.personaType().toString());
        guestProfileForSave.put("lang", guestRequest.lang());
        guestProfileForSave.put("isPrivacyPolicyAgreed", String.valueOf(guestRequest.isPrivacyPolicyAgreed()));
        guestProfileForSave.put("isLocationServiceAgreed", String.valueOf(guestRequest.isLocationServiceAgreed()));
        guestProfileForSave.put("isTermsOfServiceAgreed", String.valueOf(guestRequest.isTermsOfServiceAgreed()));
        guestProfileForSave.put("allTermsAgreedAt", LocalDateTime.now().toString());

        String key = "GUEST_PROFILE:" + guestId;
        redisTemplate.opsForHash().putAll(key, guestProfileForSave);
        redisTemplate.expire(key, Duration.ofDays(14));
        log.info("[Auth] 게스트 프로필 Redis 저장 완료 (Guest ID: {})", guestId);

        //3. Access/Refresh Token을 만든다
        long accessTime = 60 * 60 * 1000L; // 1시간
        long refreshTime = 14 * 24 * 60 * 60 * 1000L; // 14일
        String accessToken = jwtUtil.createJwt(guestId, Role.GUEST.getValue(), accessTime);
        String refreshToken = jwtUtil.createJwt(guestId, Role.GUEST.getValue(), refreshTime);

        //4. Redis에 Refresh Token을 저장한다.
        redisTemplate.opsForValue()
                .set("RT:" + guestId, refreshToken, 14, TimeUnit.DAYS);
        log.info("[Auth] 게스트 Refresh Token Redis 저장 완료 (Guest ID: {})", guestId);

        // 반환을 위한 객체 조립
        Tokens tokens = new Tokens(accessToken, refreshToken, Role.GUEST.getValue(),null);
        LoginDto.GuestProfile guestProfile = new LoginDto.GuestProfile(
                "게스트" + guestId.substring(0, 4),
                guestRequest.personaType(),
                guestRequest.lang()
        );

        return new LoginDto.GuestLoginResult(tokens, guestProfile);
    }

    //redis에 저장되어 있는 게스트 정보 가져오기
    public LoginDto.GuestProfile getProfileInRedis(String guestId) {
        String key = "GUEST_PROFILE:" + guestId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            log.warn("[Auth] 게스트 정보 Redis에서 찾을 수 없음 (Guest ID: {})", guestId);
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "게스트 정보를 찾을 수 없습니다.");
        }
        log.info("[Auth] 게스트 정보 Redis 조회 성공 (Guest ID: {})", guestId);

        String personaStr = (String) redisTemplate.opsForHash().get(key, "persona");
        String langStr = (String) redisTemplate.opsForHash().get(key, "lang");

        PersonaType personaType;
        switch (personaStr) {
            case "FOX":
                personaType = PersonaType.FOX;
                break;
            case "BEAR":
                personaType = PersonaType.BEAR;
                break;
            case "PANDA":
                personaType = PersonaType.PANDA;
                break;
            case "CAT":
                personaType = PersonaType.CAT;
                break;
            default:
                personaType = null;
        }

        LoginDto.GuestProfile guestProfile = new LoginDto.GuestProfile(
                "게스트" + guestId.substring(0, 4),
                personaType,
                langStr);
        return guestProfile;
    }

    public LoginDto.Response adminLogin(LoginDto.AdminRequest adminRequest) {
        // 1. 아이디 검증
        if (!"admin".equals(adminRequest.id())) {
            log.warn("[Auth] 관리자 로그인 실패: 존재하지 않는 ID ({})", adminRequest.id());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "관리자 로그인 정보가 일치하지 않습니다.");
        }

        // 2. 비밀번호 검증
        if (!adminPassword.equals(adminRequest.password())) {
            log.warn("[Auth] 관리자 로그인 실패: 비밀번호 불일치");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "관리자 로그인 정보가 일치하지 않습니다.");
        }

        // 3. 토큰 생성 및 응답
        long accessTime = 60 * 60 * 1000L; // 1시간
        String roleValue = Role.ADMIN.getValue();
        String accessToken = jwtUtil.createAdminJwt(adminRequest.id(), roleValue, accessTime);

        log.info("[Auth] 관리자 로그인 성공 (ID: {})", adminRequest.id());

        return new LoginDto.Response(accessToken, roleValue, null, null, null, null);
    }
}
