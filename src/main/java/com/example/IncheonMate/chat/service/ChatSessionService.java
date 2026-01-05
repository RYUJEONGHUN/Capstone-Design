package com.example.IncheonMate.chat.service;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.dto.ChatSessionResponse;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
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
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_SESSION_NOT_FOUND,chatSessionId + "에 해당하는 채팅 세션을 찾을 수 없습니다."));

        log.info("'{}' 채팅 세션 조회 완료: {}", email, chatSessionId);
        return ChatSessionResponse.DetailDto.from(targetChatSession);
    }

    public List<ChatSessionResponse.SearchedMessageDto> searchMessagesByKeyword(String email, String keyword) {
        //검색어가 있는지 검사
        if(keyword == null || keyword.trim().isEmpty()){
            log.warn("검색어가 없습니다.");
            throw new CustomException(ErrorCode.INVALID_KEYWORD_VALUE);
        }

        //'내 채팅'중에서만 검색
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
        List<ChatSession> allChatSessionIncludingKeyword = chatSessionRepository.findByMemberIdAndMessagesContentContaining(targetMember.getId(),keyword);
        
        //세션 리스트에서 키워드를 포함하는 메시지만 꺼내고 dto에 넣어 리턴
        //메시지 개수가 처리하지 못할 정도로 많아지면 MongoTemplate로 리팩터링
        return allChatSessionIncludingKeyword.stream()
                .flatMap(session -> session.getMessages().stream())
                .filter(msg -> msg.getContent().contains(keyword))
                .map(ChatSessionResponse.SearchedMessageDto::from)
                .toList();
    }
}
