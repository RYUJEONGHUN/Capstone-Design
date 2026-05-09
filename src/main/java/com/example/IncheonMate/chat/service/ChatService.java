package com.example.IncheonMate.chat.service;

import com.example.IncheonMate.chat.client.AiChatClient;
import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;
import com.example.IncheonMate.chat.domain.type.ChatResponseProvider;
import com.example.IncheonMate.chat.domain.type.ChatResponseType;
import com.example.IncheonMate.chat.dto.ChatRequest;
import com.example.IncheonMate.chat.dto.ChatResponse;
import com.example.IncheonMate.chat.dto.FastApi;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.chat.repository.GuestChatSessionRepository;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
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

    public ChatResponse.CurrentDto getCurrentChat(String identifier, boolean isGuest) {
        log.debug("[Chat] 오늘 채팅 내역 조회 시작");
        //게스트 -> 채팅 횟수에 제한이 있어서 남은 횟수를 돌려줘야한다.
        if (isGuest) {
            //게스트의 채팅 내역을 Redis에서 꺼내온다.
            Optional<GuestChatSession> guestChatSessionOpt = guestChatSessionRepository.findById(identifier);

            //1. 기존 채팅 내역이 있을 경우
            if (guestChatSessionOpt.isPresent()) {
                GuestChatSession guestChatSession = guestChatSessionOpt.get();
                log.info("[Chat] 게스트 기존 채팅 내역 있음");

                //안전한 remainingCount값 가져오기
                String countStr = redisTemplate.opsForValue().get("GUEST_COUNT:" + identifier);
                int remainingCount = (countStr != null) ? Integer.parseInt(countStr) : 0;

                return ChatResponse.CurrentDto.of(
                        guestChatSession.getId(),
                        guestChatSession.getTitle(),
                        guestChatSession.getMessages().stream()
                                .map(ChatResponse.MessageDto::fromGuest)
                                .toList(),//guestChatSession에 있는 List<Message>를 stream해야한다.들어가는 변수가 List<ChatResponse.MessageDto> 형태여야하기 때문에
                        remainingCount);
            }

            //2. 기존 채팅 내역이 없을 경우
            log.info("[Chat] 게스트 기존 채팅 내역 없음");
            return ChatResponse.CurrentDto.of(
                    null,
                    null,
                    Collections.emptyList(),
                    10
            );
        }


        //정회원 -> 채팅 횟수에 제한이 없다. remainingChatCount는 null이다.

        //1. 회원의 ID를 가져온다.
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);
        //2. 가장 최근에 생성한 채팅 세션을 찾는다
        Optional<ChatSession> currentChatSessionOpt = chatSessionRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId);

        //3. 정회원의 최근 채팅 기록이 있으면 대화 내용 리턴
        if (currentChatSessionOpt.isPresent()) {
            log.info("[Chat] 정회원 최근 채팅 기록 있음(SessionId: {})", currentChatSessionOpt.get().getId());
            return ChatResponse.CurrentDto.fromMember(currentChatSessionOpt.get());
        }

        //3. 정회원의 최근 채팅 기록이 '없'으면 대화 내용이 없으니 null을 리턴
        //채팅을 한 적이 전혀 없는 회원이기 때문에 POST:/api/chat AI로부터 답변 받아 올 때 새로운 채팅 세션 생성함
        log.info("[Chat] 정회원 최근 채팅 기록 없음");
        return ChatResponse.CurrentDto.of(
                null,
                null,
                Collections.emptyList(),
                null
        );

    }


    //AI에게 채팅 응답 요청하고 받아서 저장,프론트 응답
    //나중에 AiService,GuestPolicyService로 나누는 리팩토링 필요
    @Transactional
    public ChatResponse.Generation sendChatMessage(String identifier, boolean isGuest, ChatRequest.MessageDto messageDto) {
        log.debug("[Chat] 채팅 메시지 전송 시작");


        //게스트인 경우
        if(isGuest){
            String countKey = "GUEST_COUNT:" + identifier;

            //1. 키가 없으면 최조 채팅이므로 Redis에 초기값 10할당(TTL 14일)
            if(Boolean.FALSE.equals(redisTemplate.hasKey(countKey))){
                redisTemplate.opsForValue().set(countKey, "10", 14,TimeUnit.DAYS);
                log.debug("[Chat] 게스트 최초 채팅 진행. 남은 횟수 초기화(Initial Count: 10)");
            }

            //2. 채팅 횟수 1 차감 후 남은 횟수 확인
            Long remainingCount = redisTemplate.opsForValue().decrement(countKey);

            //3. 차감된 결과가 0미만이면 한도를 초과함
            if(remainingCount != null && remainingCount < 0){
                redisTemplate.opsForValue().increment(countKey);//차감된 것 복구
                log.info("[Chat] 게스트 채팅 횟수 초과");
                throw new CustomException(ErrorCode.GUEST_CHAT_LIMIT_EXCEEDED);
            }
            log.debug("[Chat] 게스트 채팅 횟수 감소 완료 (RemainingCount: {})", remainingCount);

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
            FastApi.ChatResponseDto chatResponseDto = null;
            try {
                chatResponseDto = getAnswerMessageFromFastApi(identifier, true, messageDto.message());
            }catch (Exception e){
                if (Boolean.TRUE.equals(redisTemplate.hasKey(countKey))) {
                    redisTemplate.opsForValue().increment(countKey);
                    log.debug("[Chat] AI 서버 응답 오류로 인해 게스트 채팅 횟수 복구 완료");
                }
                throw e;
            }

            //5. response type이 chat이나 search 이면 채팅 저장
            //게스트는 course 생성하지 못함
            if(ChatResponseType.CHAT.name().equalsIgnoreCase(chatResponseDto.fastApiChatResponseType()) ||
                    ChatResponseType.SEARCH.name().equalsIgnoreCase(chatResponseDto.fastApiChatResponseType())) {

                log.debug("[Chat] FastAPI 게스트 채팅 응답 성공 (Type: CHAT/SEARCH)");

                //6. AI Message 엔티티 생성
                GuestChatSession.Message aiMessage = GuestChatSession.Message.builder()
                        .id(UUID.randomUUID().toString())
                        .messagedAt(LocalDateTime.now())
                        .authorType(AuthorType.AI)
                        .content(chatResponseDto.answer())
                        .chatResponseType(
                                chatResponseDto.fastApiChatResponseType() != null
                                        ? ChatResponseType.valueOf(chatResponseDto.fastApiChatResponseType().toUpperCase())
                                        : null
                        )
                        .chatResponseProvider(
                                chatResponseDto.fastApiChatProvider() != null
                                        ? ChatResponseProvider.valueOf(chatResponseDto.fastApiChatProvider().toUpperCase())
                                        : null
                        )
                        .build();


                //7. GuestChatSession 엔티티의 LastMessageAt 업데이트 하고 Message 엔티티 추가
                guestChatSession.addMessages(userMessage,aiMessage);
                //8. 저장
                guestChatSessionRepository.save(guestChatSession);
                log.debug("[Chat] 게스트 채팅 메시지 저장 완료");

                return ChatResponse.Generation.fromGuest(userMessage,aiMessage);
            }else if(ChatResponseType.COURSE.name().equalsIgnoreCase(chatResponseDto.fastApiChatResponseType()) ||
                Boolean.TRUE.equals(chatResponseDto.isCourse())){
                log.debug("[Chat] 코스 생성 요청 감지 (게스트 불가) - 이용 횟수 복구 및 안내 메시지 반환");

                GuestChatSession.Message systemFallbackMessage = GuestChatSession.Message.builder()
                        .id(UUID.randomUUID().toString())
                        .messagedAt(LocalDateTime.now())
                        .authorType(AuthorType.AI)
                        .content("게스트 모드에서는 맞춤형 여행 코스 생성 기능을 사용할 수 없습니다. 회원가입 후 더 많은 기능을 누려보세요!")
                        .chatResponseType(
                                chatResponseDto.fastApiChatResponseType() != null
                                        ? ChatResponseType.valueOf(chatResponseDto.fastApiChatResponseType().toUpperCase())
                                        : null
                        )
                        .chatResponseProvider(
                                chatResponseDto.fastApiChatProvider() != null
                                        ? ChatResponseProvider.valueOf(chatResponseDto.fastApiChatProvider().toUpperCase())
                                        : null
                        )
                        .build();

                guestChatSession.addMessages(userMessage, systemFallbackMessage);
                guestChatSessionRepository.save(guestChatSession);

                redisTemplate.opsForValue().increment(countKey);//게스트 남은 채팅 횟수 복구
                return ChatResponse.Generation.fromGuest(userMessage, systemFallbackMessage);
            }


        //정회원인 경우
        }else{
            //1. 유저 Message 엔티티 생성
            ChatSession.Message userMessage = ChatSession.Message.builder()
                    .id(UUID.randomUUID().toString())
                    .messagedAt(LocalDateTime.now())
                    .authorType(AuthorType.USER)
                    .content(messageDto.message())
                    .build();

            //2. 최근 채팅 세션이 있으면 받아오고 아예 없으면 채팅 세션 새로 생성
            ChatSession chatSession = getOrCreateChatSession(identifier);

            //3. FastAPI에서 message의 결과 받아옴
            FastApi.ChatResponseDto chatResponseDto = getAnswerMessageFromFastApi(identifier,false,messageDto.message());

            //4.1 코스 생성 요청인 경우
            if(ChatResponseType.COURSE.name().equalsIgnoreCase(chatResponseDto.fastApiChatResponseType()) &&
                    Boolean.TRUE.equals(chatResponseDto.isCourse())) {
                //String 파싱하여 코스 저장하고, 코스에 맞는 x,y좌표 및 정보를 fastapi 채팅 응답과 합쳐서 저장하는 메서드 만들어야함
                return null;
            }
            //4.2 코스 생성 요청이 아닌 경우
            else if(ChatResponseType.CHAT.name().equalsIgnoreCase(chatResponseDto.fastApiChatResponseType()) ||
                    ChatResponseType.SEARCH.name().equalsIgnoreCase(chatResponseDto.fastApiChatResponseType())) {

                //4. AI Message 엔티티 생성
                ChatSession.Message aiMessage = ChatSession.Message.builder()
                        .id(UUID.randomUUID().toString())
                        .messagedAt(LocalDateTime.now())
                        .authorType(AuthorType.AI)
                        .content(chatResponseDto.answer())
                        .chatResponseType(
                                chatResponseDto.fastApiChatResponseType() != null
                                        ? ChatResponseType.valueOf(chatResponseDto.fastApiChatResponseType().toUpperCase())
                                        : null
                        )
                        .chatResponseProvider(
                                chatResponseDto.fastApiChatProvider() != null
                                        ? ChatResponseProvider.valueOf(chatResponseDto.fastApiChatProvider().toUpperCase())
                                        : null
                        )
                        .build();

                //5. ChatSession 엔티티의 LastMessageAt 업데이트 하고 Message 엔티티 추가
                chatSession.addMessages(userMessage, aiMessage);
                //6. 저장
                chatSessionRepository.save(chatSession);
                log.debug("[Chat] 정회원 채팅 메시지 저장 완료");

                return ChatResponse.Generation.fromUser(userMessage, aiMessage);
            }
        }

        log.warn("[Chat] 정의되지 않은 FastAPI 응답 타입 수신");

        //guest인 경우 채팅 횟수 복구
        if (isGuest) {
            String countKey = "GUEST_COUNT:" + identifier;
            // 키가 존재할 때만 안전하게 복구 (Atomic 증가)
            if (Boolean.TRUE.equals(redisTemplate.hasKey(countKey))) {
                redisTemplate.opsForValue().increment(countKey);
                log.debug("[Chat] 알 수 없는 응답 타입으로 인해 게스트 채팅 횟수 복구 완료 (Key: {})", countKey);
            }
        }

        //예외 처리
        throw new CustomException(ErrorCode.AI_SERVER_ERROR);
    }

    private record UserAiTraitsDto(String persona, String mbti, String sasang) {}

    private UserAiTraitsDto getUserAiTraits(String identifier, boolean isGuest) {
        if(isGuest){
            Object personaObj = redisTemplate.opsForHash().get("GUEST_PROFILE:" + identifier, "persona");
            if (personaObj == null) {
                log.warn("[Chat] 게스트 프로필에 페르소나 정보 없음");
                throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "게스트 정보를 찾을 수 없습니다.");
            }
            return new UserAiTraitsDto(personaObj.toString(), null,null);

        }else{
            Member targetMember = memberRepository.getMemberByEmail(identifier);

            // NPE 방어 로직: null인지 먼저 확인 후 메서드 호출
            String persona = targetMember.getSelectedPersona() != null ? targetMember.getSelectedPersona().toString() : null;
            String mbti = targetMember.getMbti() != null ? targetMember.getMbti().toString() : null;
            String sasang = targetMember.getSasang() != null ? targetMember.getSasang().getDescription() : null;

            return new UserAiTraitsDto(persona, mbti, sasang);
        }
    }

    private FastApi.ChatResponseDto getAnswerMessageFromFastApi(String identifier, boolean isGuest, String userMessage){
        //필요 데이터: input_message, 사용자 식별자(이메일,게스트uuid), 페르소나, mbti, sasang

        //1. FastAPI로 전송할 데이터 준비
        UserAiTraitsDto userData = getUserAiTraits(identifier,isGuest);

        //2. OpenFeign으로 FastAPI에 채팅 생성 요청 보내기
        log.debug("[Chat] FastAPI 채팅 요청 전송");
        FastApi.ChatResponseDto chatResponseDto = aiChatClient.getAnswerMessage(FastApi.ChatRequestDto.of(userMessage,identifier,userData.persona,userData.mbti,userData.sasang));
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
                    log.info("[Chat] 게스트 채팅 세션이 없음-신규 채팅 세션 생성");
                    LocalDateTime now = LocalDateTime.now();

                    GuestChatSession newSession = GuestChatSession.builder()
                            .id(identifier)
                            .title("Guest" + identifier.substring(0, 4) + "-Chat")
                            .createdAt(now)
                            .lastMessageAt(now)
                            .build();

                    log.info("[Chat] 게스트 채팅 세션 생성 완료");
                    return guestChatSessionRepository.save(newSession);
                });
    }


    //가장 최근에 생성된 세션 1개를 가져오거나 생성함
    @Transactional
    private ChatSession getOrCreateChatSession(String identifier){
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);

        return chatSessionRepository.findFirstByMemberIdOrderByCreatedAtDesc(memberId)

                .orElseGet(() -> {
                    log.info("[Chat] 정회원 채팅 세션이 없음-신규 채팅 세션 생성");
                    LocalDateTime now = LocalDateTime.now();

                    ChatSession newSession = ChatSession.builder()
                            .title(LocalDate.now().toString())
                            .createdAt(now)
                            .lastMessageAt(now)
                            .memberId(memberId)
                            .build();

                    log.info("[Chat] 정회원 채팅 세션 생성 완료");
                    return chatSessionRepository.save(newSession);
                });
    }

}
