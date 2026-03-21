package com.example.IncheonMate.chat.service;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.chat.dto.ChatResponse;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.chat.repository.GuestChatSessionRepository;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Collections;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final MemberRepository memberRepository;
    private final GuestChatSessionRepository guestChatSessionRepository;
    private final StringRedisTemplate redisTemplate;

    public ChatResponse.TodayDto getTodayChat(String identifier, boolean isGuest){
        //게스트 -> 채팅 횟수에 제한이 있어서 남은 횟수를 돌려줘야한다.
        if(isGuest){
            //게스트의 채팅 내역을 Redis에서 꺼내온다.
            Optional<GuestChatSession> guestChatSessionOpt = guestChatSessionRepository.findById(identifier);
            String guestChatTitle = "Guest"+identifier.substring(0,4)+"-Chat";

            //1. 기존 채팅 내역이 있을 경우
            if(guestChatSessionOpt.isPresent()) {
                GuestChatSession guestChatSession = guestChatSessionOpt.get();
                return ChatResponse.TodayDto.of(
                        guestChatSession.getId(),
                        guestChatTitle,
                        guestChatSession.getMessages().stream()
                                .map(ChatResponse.MessageDto::fromGuest)
                                .toList(),//guestChatSession에 있는 List<Message>를 stream해야한다.들어가는 변수가 List<ChatResponse.MessageDto> 형태여야하기 때문에
                        getRemainingCount(identifier));
            }

            //2. 기존 채팅 내역이 없을 경우
            return ChatResponse.TodayDto.of(
                    null,
                    guestChatTitle,
                    Collections.emptyList(),
                    getRemainingCount(identifier)
            );
        }


        //정회원 -> 채팅 횟수에 제한이 없다. remainingChatCount는 null이다.

        //1. 회원의 ID를 가져온다.
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);
        //2. ID기반으로 채팅 세션중 생성일이 오늘인 세션을 찾는다.
        LocalDateTime dayOfStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayOfEnd = LocalDate.now().atTime(LocalTime.MAX);
        Optional<ChatSession> todayChatSessionOpt = chatSessionRepository.findTodaySessionByMemberId(memberId,dayOfStart,dayOfEnd);

        //3. 정회원의 오늘 채팅 기록이 있으면 대화 내용 리턴
        if(todayChatSessionOpt.isPresent()) {
            return ChatResponse.TodayDto.fromMember(todayChatSessionOpt.get());
        }

        //3. 정회원의 오늘 채팅 기록이 '없'으면 대화 내용이 없으니 null을 리턴
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
            return 10;
        }

        //2. 키가 Redis에 있으면 채팅을 하고 있던 게스트이므로 저장값 반환
        String count = (String) redisTemplate.opsForValue().get(key);
        return Integer.parseInt(count);
    }
}
