package com.example.IncheonMate.chat.service;

import com.example.IncheonMate.chat.client.AiChatClient;
import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;
import com.example.IncheonMate.chat.dto.ChatRequest;
import com.example.IncheonMate.chat.dto.ChatResponse;
import com.example.IncheonMate.chat.dto.FastApi;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.chat.repository.GuestChatSessionRepository;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.type.PersonaType;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Collections;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final MemberRepository memberRepository;
    private final GuestChatSessionRepository guestChatSessionRepository;
    private final StringRedisTemplate redisTemplate;
    private final AiChatClient aiChatClient;

    public ChatResponse.TodayDto getTodayChat(String identifier, boolean isGuest){
        log.debug("[Chat] 오늘 채팅 내역 조회 시작");
        //게스트 -> 채팅 횟수에 제한이 있어서 남은 횟수를 돌려줘야한다.
        if(isGuest){
            //게스트의 채팅 내역을 Redis에서 꺼내온다.
            Optional<GuestChatSession> guestChatSessionOpt = guestChatSessionRepository.findById(identifier);

            //1. 기존 채팅 내역이 있을 경우
            if(guestChatSessionOpt.isPresent()) {
                GuestChatSession guestChatSession = guestChatSessionOpt.get();
                log.info("[Chat] 게스트 기존 채팅 내역 있음");
                return ChatResponse.TodayDto.of(
                        guestChatSession.getId(),
                        guestChatSession.getTitle(),
                        guestChatSession.getMessages().stream()
                                .map(ChatResponse.MessageDto::fromGuest)
                                .toList(),//guestChatSession에 있는 List<Message>를 stream해야한다.들어가는 변수가 List<ChatResponse.MessageDto> 형태여야하기 때문에
                        getRemainingCount(identifier));
            }

            //2. 기존 채팅 내역이 없을 경우
            log.info("[Chat] 게스트 기존 채팅 내역 없음");
            return ChatResponse.TodayDto.of(
                    null,
                    null,
                    Collections.emptyList(),
                    getRemainingCount(identifier)
            );
        }


        //정회원 -> 채팅 횟수에 제한이 없다. remainingChatCount는 null이다.

        //1. 회원의 ID를 가져온다.
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);
        //2. ID기반으로 채팅 세션중 생성일이 오늘인 세션을 찾는다.
        LocalDateTime startOfToday = getStartOfToday();
        LocalDateTime endOfToday = getEndOfToday();
        Optional<ChatSession> todayChatSessionOpt = chatSessionRepository.findTodaySessionByMemberId(memberId,startOfToday,endOfToday);

        //3. 정회원의 오늘 채팅 기록이 있으면 대화 내용 리턴
        if(todayChatSessionOpt.isPresent()) {
            log.info("[Chat] 정회원 오늘 채팅 기록 있음");
            return ChatResponse.TodayDto.fromMember(todayChatSessionOpt.get());
        }

        //3. 정회원의 오늘 채팅 기록이 '없'으면 대화 내용이 없으니 null을 리턴
        log.info("[Chat] 정회원 오늘 채팅 기록 없음");
        return ChatResponse.TodayDto.of(
                null,
                null,
                Collections.emptyList(),
                null
        );

    }

    private int getRemainingCount(String guestId){
        String key = "GUEST_COUNT:" + guestId;

        //1. 키가 Redis에 없으면 채팅을 처음 시작하는 게스트이므로 생성 및 10할당(TTL 14일)
        if(!redisTemplate.hasKey(key)){
            redisTemplate.opsForValue().set(key,"10",14, TimeUnit.DAYS);
            log.debug("[Chat] 게스트 채팅 횟수 초기화 (Count: 10)");
            return 10;
        }

        //2. 키가 Redis에 있으면 채팅을 하고 있던 게스트이므로 저장값 반환
        String count = (String) redisTemplate.opsForValue().get(key);
        log.debug("[Chat] 게스트 남은 채팅 횟수 조회 (Count: {})", count);
        return Integer.parseInt(count);
    }

    //AI에게 채팅 응답 요청하고 받아서 저장,프론트 응답
    //나중에 AiService,GuestPolicyService로 나누는 리팩토링 필요
    @Transactional
    public ChatResponse.Generation sendChatMessage(String identifier, boolean isGuest, ChatRequest.MessageDto messageDto) {
        log.debug("[Chat] 채팅 메시지 전송 시작");

        if(isGuest){
            //1.게스트이면 최대 채팅횟수 초과했는지 검사
            String remainingChatCountStr = redisTemplate.opsForValue().get("GUEST_COUNT:"+identifier);
            int remainingChatCount = (remainingChatCountStr != null) ? Integer.parseInt(remainingChatCountStr) : 0;
            // 게스트: 최대 채팅 횟수 초과했으면 게스트 채팅 초과 에러 return
            if(remainingChatCount <= 0){
                log.info("[Chat] 게스트 채팅 횟수 초과");
                throw new CustomException(ErrorCode.GUEST_CHAT_LIMIT_EXCEEDED);
            }

            //2. 유저 Message 엔티티 생성
            GuestChatSession.Message userMessage = GuestChatSession.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .messagedAt(LocalDateTime.now())
                    .authorType(AuthorType.USER)
                    .content(messageDto.message())
                    .build();

            //3. 채팅 세션이 있는지 확인하고 없으면 생성
            GuestChatSession guestChatSession = getOrCreateGuestChatSession(identifier);

            //4. FastAPI에서 message의 결과 받아옴
            FastApi.ChatResponseDto chatResponseDto = getAnswerMessageFromFastApi(identifier,true,messageDto.message());

            //5. AI Message 엔티티 생성
            GuestChatSession.Message aiMessage = GuestChatSession.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .messagedAt(LocalDateTime.now())
                    .authorType(AuthorType.AI)
                    .content(chatResponseDto.answer())
                    .build();

            //6. GuestChatSession 엔티티의 LastMessageAt 업데이트 하고 Message 엔티티 추가
            guestChatSession.addMessages(userMessage,aiMessage);
            //7. 저장
            guestChatSessionRepository.save(guestChatSession);
            log.debug("[Chat] 게스트 채팅 메시지 저장 완료");

            //8. Redis에 있는 guest count 1감소
            Long remainingCount = redisTemplate.opsForValue().decrement("GUEST_COUNT:"+identifier);
            log.debug("[Chat] 게스트 채팅 횟수 감소 (RemainingCount: {})", remainingCount);

            return ChatResponse.Generation.fromGuest(userMessage,aiMessage);

        }else{
            //1. 유저 Message 엔티티 생성
            ChatSession.Message userMessage = ChatSession.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .messagedAt(LocalDateTime.now())
                    .authorType(AuthorType.USER)
                    .content(messageDto.message())
                    .build();

            //2. 오늘자로 생성된 채팅 세션이 있는지 확인하고 없으면 생성
            ChatSession chatSession = getOrCreateChatSession(identifier);

            //3. FastAPI에서 message의 결과 받아옴
            FastApi.ChatResponseDto chatResponseDto = getAnswerMessageFromFastApi(identifier,false,messageDto.message());

            //4. AI Message 엔티티 생성
            ChatSession.Message aiMessage = ChatSession.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .messagedAt(LocalDateTime.now())
                    .authorType(AuthorType.AI)
                    .content(chatResponseDto.answer())
                    .build();

            //5. ChatSession 엔티티의 LastMessageAt 업데이트 하고 Message 엔티티 추가
            chatSession.addMessages(userMessage,aiMessage);
            //6. 저장
            chatSessionRepository.save(chatSession);
            log.debug("[Chat] 정회원 채팅 메시지 저장 완료");

            return ChatResponse.Generation.fromUser(userMessage,aiMessage);
        }


    }

    private String getPersonaType(String identifier, boolean isGuest) {
        if(isGuest){
            Object personaObj = redisTemplate.opsForHash().get("GUEST_PROFILE:" + identifier, "persona");
            if (personaObj == null) {
                log.warn("[Chat] 게스트 프로필에 페르소나 정보 없음");
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "게스트 정보를 찾을 수 없습니다.");
            }
            return personaObj.toString();
        }else{
            return memberRepository.findByEmailOrElseThrow(identifier).getSelectedPersona().toString();
        }
    }

    private FastApi.ChatResponseDto getAnswerMessageFromFastApi(String identifier, boolean isGuest, String userMessage){
        //1. FastAPI로 전송할 데이터 준비
        String personaType = getPersonaType(identifier,isGuest);

        //2. OpenFeign으로 FastAPI에 채팅 생성 요청 보내기
        log.debug("[Chat] FastAPI 채팅 요청 전송");
        FastApi.ChatResponseDto chatResponseDto = aiChatClient.getAnswerMessage(FastApi.ChatRequestDto.of(userMessage,identifier,personaType));
        //3. error 응답이 오면 exception throw
        if(!StringUtils.hasText(chatResponseDto.answer())){
            log.warn("[Chat] FastAPI 응답 오류");
            throw new CustomException(ErrorCode.AI_SERVER_ERROR);
        }

        return chatResponseDto;
    }

    //생성된 게스트 채팅 세션을 불러오고 없으면 새로 생성
    @Transactional
    private GuestChatSession getOrCreateGuestChatSession(String identifier) {
        return guestChatSessionRepository.findById(identifier)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();

                    GuestChatSession newSession = GuestChatSession.builder()
                            .id(identifier)
                            .title("Guest" + identifier.substring(0, 4) + "-Chat")
                            .createdAt(now)
                            .lastMessageAt(now)
                            .build();

                    log.info("[Chat] 게스트 채팅 세션 생성");
                    return guestChatSessionRepository.save(newSession);
                });
    }


    //오늘자로 생성된 정회원 채팅 세션이 있으면 불러오고 없으면 새로 생성
    @Transactional
    private ChatSession getOrCreateChatSession(String identifier){
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);

        return chatSessionRepository.findTodaySessionByMemberId(memberId,getStartOfToday(),getEndOfToday())

                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();

                    ChatSession newSession = ChatSession.builder()
                            .title(LocalDate.now().toString())
                            .createdAt(now)
                            .lastMessageAt(now)
                            .memberId(memberId)
                            .build();

                    log.info("[Chat] 정회원 채팅 세션 생성");
                    return chatSessionRepository.save(newSession);
                });
    }

    private LocalDateTime getStartOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime getEndOfToday() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }
}
