package com.example.IncheonMate.chat.service;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.dto.ChatSessionResponse;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
//마이페이지나 채팅 목록 화면에서 필요한 "관리" 기능
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final MemberRepository memberRepository;

    //2. 메인 -> 채팅 기록:어떤 채팅을 했는지 세션 정보만 보내줌 => Get getChatSessionSummary |도메인:chatSession
    public List<ChatSessionResponse.SummaryDto> getChatSessionSummaries(String email) {
        //멤버 ID에 있는 모든 채팅 세션 꺼내오기
        //findIdByEamil로 찾으면 member전체를 다 가져와서 채팅 세션을 못 찾음
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        String targetMemberId = targetMember.getId();
        List<ChatSession> chatSessions = chatSessionRepository.findAllByMemberId(targetMemberId);

        log.info("'{}' 채팅 세션 {}개 조회 완료", email, chatSessions.size());
        return chatSessions.stream()
                .map(ChatSessionResponse.SummaryDto::from)
                .toList();
    }

    public ChatSessionResponse.DetailDto getChatSessionDetails(String email, String chatSessionId) {
        //특정 채팅 세션 꺼내오기
        ChatSession targetChatSession = chatSessionRepository.findById(chatSessionId)
                .orElseThrow(() -> new NoSuchElementException(chatSessionId + "에 해당하는 채팅 세션을 찾을 수 없습니다."));

        log.info("'{}' 채팅 세션 조회 완료: {}", email, chatSessionId);
        return ChatSessionResponse.DetailDto.from(targetChatSession);
    }

    public List<ChatSessionResponse.SearchedMessageDto> searchMessagesByKeyword(String email, String keyword) {
        if(keyword == null || keyword.trim().isEmpty()){
            log.info("keyword가 null이거나 공백입니다.");
            return Collections.emptyList();
        }
        //키워드를 포함하는 모든 세션 꺼내오기
        List<ChatSession> allChatSessionIncludingKeyword = chatSessionRepository.findByMessagesContentContaining(keyword);
        
        //세션 리스트에서 키워드를 포함하는 메시지만 꺼내고 dto에 넣어 리턴
        //메시지 개수가 처리하지 못할 정도로 많아지면 MongoTemplate로 리팩터링
        return allChatSessionIncludingKeyword.stream()
                .flatMap(session -> session.getMessages().stream())
                .filter(msg -> msg.getContent().contains(keyword))
                .map(ChatSessionResponse.SearchedMessageDto::from)
                .toList();
    }
}
