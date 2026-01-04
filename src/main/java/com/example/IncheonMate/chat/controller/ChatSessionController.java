package com.example.IncheonMate.chat.controller;
/*
------------MyInfo---------------------------------
//2. 메인 -> 채팅 기록:어떤 채팅을 했는지 세션 정보만 보내줌 => Get getChatSessionSummary |도메인:chatSession
5. 채팅 기록 -> 세부 채팅 기록: 선택한 채팅 세션에 해당하는 채팅 전체를 보내줌 => Get getChatSessionDetail |도메인: chatSession
6. 채팅 기록 -> 키워드 검색: 전체 채팅 세션에서 검색한 결과를 보내줌 => Get + URI searchChatHistory |도메인: chatSession
 */

import com.example.IncheonMate.chat.service.ChatSessionService;
import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.IncheonMate.chat.dto.ChatSessionResponse;

import java.util.List;

//마이페이지나 채팅 목록 화면에서 필요한 "관리" 기능.
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat-sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    //2. 메인 -> 채팅 기록:어떤 채팅을 했는지 세션 정보만 보내줌 => Get getChatSessionSummary |도메인:chatSession
    @GetMapping
    public ResponseEntity<List<ChatSessionResponse.SummaryDto>> getChatSessionSummary(@AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' 채팅 기록 조회 요청", email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(chatSessionService.getChatSessionSummaries(email));
    }

    //5. 채팅 기록 -> 세부 채팅 기록: 선택한 채팅 세션에 해당하는 채팅 전체를 보내줌 => Get getChatSessionDetail |도메인: chatSession
    @GetMapping("/{chat-session-id}")
    public ResponseEntity<ChatSessionResponse.DetailDto> getChatSessionDetail(@AuthenticationPrincipal CustomOAuth2User user,
                                                                                    @PathVariable("chat-session-id") String chatSessionId){
        String email = user.getEmail();
        log.info("'{}' 채팅세션({}) 메시지 조회 요청",email,chatSessionId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(chatSessionService.getChatSessionDetails(email, chatSessionId));
    }

    //6. 채팅 기록 -> 키워드 검색: 전체 채팅 세션에서 검색한 결과를 보내줌 => Get + URI searchChatSessionDetail |도메인: chatSession
    @GetMapping("/search")
    public ResponseEntity<List<ChatSessionResponse.SearchedMessageDto>> searchChatSessionDetail(@AuthenticationPrincipal CustomOAuth2User user,
                                                                                                @RequestParam("keyword") String keyword) {
        String email = user.getEmail();
        log.info("'{}' \"{}\"을(를) 포함하는 메시지 조회 요청",email,keyword);

        return ResponseEntity.status(HttpStatus.OK)
                .body(chatSessionService.searchMessagesByKeyword(email,keyword));
    }
}
