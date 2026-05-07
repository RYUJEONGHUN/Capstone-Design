package com.example.IncheonMate.member.service;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.chat.repository.GuestChatSessionRepository;
import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.common.jwt.JWTUtil;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.Role;
import com.example.IncheonMate.member.dto.*;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.member.domain.type.SasangType;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final MemberCommonService memberCommonService;
    private final StringRedisTemplate redisTemplate;
    private final GuestChatSessionRepository guestChatSessionRepository;
    private final ChatSessionRepository chatSessionRepository;
    //현재 약관 버전
    private static final String CURRENT_TERMS_VERSION = "v1.0.0";

    //사상의학 테스트 결과 도출
    public MemberCommonDto.SasangResponseDto deriveSasangResult(List<MemberCommonDto.SasangAnswerDto> testResult, String email) {
        //체질 도출 로직
        SasangType sasangType = memberCommonService.analyzeSasangType(testResult);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        return new MemberCommonDto.SasangResponseDto(email, sasangType);
    }

    //초기 입력 화면(온보딩)에서 사용자가 입력한 정보 전체의 정책을 검사하고 저장하는 서비스
    @Transactional
    public void saveOnboarding(String email, OnboardingBundle.OnboardingDto onboardingDto, String guestId, String provider, String name) {
        /*
        String nickname -> 최소 2글자/'사용자' 미포함
        String birthdate -> 6자리 숫자
        String gender -> Enum,not NULL
        String mbti -> 대소문자 허용
        String profileImage; -> nullable
        CompanionType companion ->not null
        SasangType sasang -> not null
        String selectedPersonaId -> not null
        lang -> kor or eng
        */
        //온보딩DTO null 검증
        if (onboardingDto == null) {
            log.warn("[Member] 온보딩 데이터가 Null임");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "저장할 온보딩 데이터가 업습니다.");
        }

        //닉네임이 정책에 맞게 들어왔는지 검증
        if (!memberCommonService.checkNicknamePolicy(onboardingDto.nickname())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, onboardingDto.nickname() + "은(는) 정책을 위배한 닉네임입니다");
        }
        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = memberCommonService.parseLocalDate(onboardingDto.birthDate());

        //새로운 멤버 생성
        Member newMember = Member.builder()
                .email(email)
                .nickname(onboardingDto.nickname())
                .provider(provider)
                .name(name)
                .birthDate(birthDate)
                .gender(onboardingDto.gender())
                .mbti(memberCommonService.parseMbti(onboardingDto.mbti()))
                .profileImageURL(onboardingDto.profileImageURL())
                .profileImageAsMarker(StringUtils.hasText(onboardingDto.profileImageURL()))
                .sasang(onboardingDto.sasang())
                .selectedPersona(onboardingDto.selectedPersona())
                .lang(onboardingDto.lang())
                .role(Role.USER.getValue())
                .isTermsOfServiceAgreed(onboardingDto.isTermsOfServiceAgreed())
                .isLocationServiceAgreed(onboardingDto.isLocationServiceAgreed())
                .isPrivacyPolicyAgreed(onboardingDto.isPrivacyPolicyAgreed())
                .allTermsAgreedAt(LocalDateTime.now())
                .termsVersion(CURRENT_TERMS_VERSION)
                .companion(onboardingDto.companion())
                .build();

        memberRepository.save(newMember);

        //게스트 계정으로 가입한 내역이 있는 멤버이면 채팅 내역도 DB에 저장해야함
        if (!"newUser".equals(guestId)) {
            if (!migrateGuestChatHistory(newMember.getId(), guestId)) {
                log.warn("[Member] 게스트 채팅 내역 마이그레이션 실패 (GuestId: {})", guestId);
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "게스트 채팅 내역 마이그레이션 실패");
            }
            //GUEST_RROFILE 삭제하기
            redisTemplate.delete("GUEST_PROFILE:" + guestId);
            //ROLE_GUEST로 만든 Refresh Token도 Redis에서 삭제하기
            redisTemplate.delete("RT:" + guestId);
            log.info("[Member] 게스트 Redis 데이터 정리 완료 (GuestId: {})", guestId);
        }

        log.info("[Member] 정회원 가입 완료");
    }

    @Transactional
    public boolean migrateGuestChatHistory(String memberId, String guestId) {
        try {
            //1. NPE 방어
            if (memberId == null || guestId == null) {
                log.warn("[Member] [Migration] memberId 또는 guestId가 null입니다.");
                return false;
            }
            //2. redis repository에서 가져오기
            Optional<GuestChatSession> guestChatSessionOpt = guestChatSessionRepository.findById(guestId);
            if (guestChatSessionOpt.isEmpty()) {
                log.warn("[Member] [Migration] 마이그레이션할 게스트 채팅 내역이 없음 (GuestId: {})", guestId);
                return false;
            }
            GuestChatSession guestChatSession = guestChatSessionOpt.get();

            //3. 이미 이전완료 되었는지 검사
            if (chatSessionRepository.existsById(guestChatSession.getId())) {
                log.info("[Member] [Migration] 이미 이전된 채팅 세션입니다. (SessionId: {})", guestChatSession.getId());
                return true;
            }

            // 4. Messages 마이그레이션
            List<ChatSession.Message> memberMessageList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(guestChatSession.getMessages())) {
                memberMessageList = guestChatSession.getMessages().stream()
                        .map(guestMessage -> ChatSession.Message.builder()
                                .id(guestMessage.getId())
                                .messagedAt(guestMessage.getMessagedAt())
                                .authorType(guestMessage.getAuthorType())
                                .content(guestMessage.getContent())
                                .chatResponseProvider(guestMessage.getChatResponseProvider())
                                .chatResponseType(guestMessage.getChatResponseType())
                                .build())
                        .collect(Collectors.toList());
            }

            //5. ChatSession 마이그레이션
            ChatSession memberChatSession = ChatSession.builder()
                    .id(guestChatSession.getId())
                    .title(guestChatSession.getTitle())
                    .createdAt(guestChatSession.getCreatedAt())
                    .lastMessageAt(guestChatSession.getLastMessageAt())
                    .memberId(memberId)
                    .messages(memberMessageList)
                    .build();

            chatSessionRepository.save(memberChatSession);
            return true;
        } catch (Exception e) {
            log.error("[Member] [Migration] 채팅 내역 이전 중 시스템 오류 발생. (GuestId: {})", guestId, e);
            return false;
        }
    }


//    //saveAgreements컨트롤러
//    //약관 동의 내역을 검사하고 모두 동의 했을때에만 저장하는 서비스
//    @Transactional
//    public OnboardingBundle.TermsAgreementResponse saveAgreements(String email, OnboardingBundle.TermsAgreementRequest termsAgreementRequest) {
//        //저장할 멤버
//        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
//        //현재 시간
//        LocalDateTime now = LocalDateTime.now();
//        //약관 버전->버전관리 필요하면 메소드 형태로 변형
//        String currentTermsVersion = CURRENT_TERMS_VERSION;
//
//        //멤버 약관 동의 저장
//        Member updatedMember =  targetMember.toBuilder()
//                .isPrivacyPolicyAgreed(termsAgreementRequest.isPrivacyPolicyAgreed())
//                .isLocationServiceAgreed(termsAgreementRequest.isLocationServiceAgreed())
//                .isTermsOfServiceAgreed(termsAgreementRequest.isTermsOfServiceAgreed())
//                .allTermsAgreedAt(now)
//                .termsVersion(currentTermsVersion)
//                .build();
//        memberRepository.save(updatedMember);
//        log.info("'{}' 약관 동의 내역 저장 완료",email);
//        return OnboardingBundle.TermsAgreementResponse.from(updatedMember);
//    }

        //getOnboardingData 서비스
        public OnboardingBundle.OnboardingDto getOnboardingValues (String email){
            OnboardingBundle.OnboardingDto result = OnboardingBundle.OnboardingDto.from(memberRepository.findByEmailOrElseThrow(email));
            log.info("[Member] 사용자 정보 조회 성공");
            return result;
        }
    }
