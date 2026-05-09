package com.example.IncheonMate.chat.controller;
/*
------------MyInfo---------------------------------
//2. 메인 -> 채팅 기록:어떤 채팅을 했는지 세션 정보만 보내줌 => Get getChatSessionSummary |도메인:chatSession
5. 채팅 기록 -> 세부 채팅 기록: 선택한 채팅 세션에 해당하는 채팅 전체를 보내줌 => Get getChatSessionDetail |도메인: chatSession
6. 채팅 기록 -> 키워드 검색: 전체 채팅 세션에서 검색한 결과를 보내줌 => Get + URI searchChatHistory |도메인: chatSession
 */

import com.example.IncheonMate.chat.service.ChatSessionService;
import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.IncheonMate.chat.dto.ChatSessionResponse;

import java.util.List;

//마이페이지나 채팅 목록 화면에서 필요한 "관리" 기능.
@Tag(name = "Chat Session API", description = "채팅 세션 관리 기능")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat-sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    //2. 메인 -> 채팅 기록:어떤 채팅을 했는지 세션 정보만 보내줌 => Get getChatSessionSummary |도메인:chatSession
    @Operation(summary = "채팅 세션 목록 조회", description = "MyInfo에서 채팅 세션 전체 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 세션 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatSessionResponse.SummaryDto.class)))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<ChatSessionResponse.SummaryDto>> getChatSessionSummary(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user) {
        String email = user.getIdentifier();
        log.info("[Chat] 채팅 세션 목록 조회 요청");

        List<ChatSessionResponse.SummaryDto> result = chatSessionService.getChatSessionSummaries(email);
        log.info("[Chat] 채팅 세션 목록 조회 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    //5. 채팅 기록 -> 세부 채팅 기록: 선택한 채팅 세션에 해당하는 채팅 전체를 보내줌 => Get getChatSessionDetail |도메인: chatSession
    @Operation(summary = "특정 채팅 세션 채팅 내역 조회", description = "MyInfo에서 한개의 채팅 세션에 대한 채팅 메시지들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 내역 조회 성공", content = @Content(schema = @Schema(implementation = ChatSessionResponse.DetailDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅 세션을 찾을 수 없습니다", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{chat-session-id}")
    public ResponseEntity<ChatSessionResponse.DetailDto> getChatSessionDetail(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user,
                                                                              @Parameter(name = "chat-session-id", description = "조회할 채팅 세션의 ID", example = "65a1b2c...", required = true) @PathVariable("chat-session-id") String chatSessionId) {
        String email = user.getIdentifier();
        log.info("[Chat] 특정 채팅 세션 채팅 내역 조회 요청 (ChatSessionId: {})",chatSessionId);

        ChatSessionResponse.DetailDto result = chatSessionService.getChatSessionDetails(email, chatSessionId);
        log.info("[Chat] 특정 채팅 세션 채팅 내역 조회 성공 (ChatSessionId: {})", chatSessionId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    //6. 채팅 기록 -> 키워드 검색: 전체 채팅 세션에서 검색한 결과를 보내줌 => Get + URI searchChatSessionDetail |도메인: chatSession
    @Operation(summary = "키워드를 포함하는 채팅 내역 조회", description = "MyInfo에서 키워드를 포함하는 모든 채팅 내역을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 내역 키워드 검색 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatSessionResponse.SearchedMessageDto.class)))),
            @ApiResponse(responseCode = "400", description = "검색어가 없습니다.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<ChatSessionResponse.SearchedMessageDto>> searchChatSessionDetail(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user,
                                                                                                @Parameter(description = "검색할 메시지 키워드", example = "맛집", required = true) @RequestParam("keyword") String keyword) {
        String email = user.getIdentifier();
        log.info("[Chat] 키워드를 포함하는 채팅 내역 조회 요청 (Keyword: {})", keyword);

        List<ChatSessionResponse.SearchedMessageDto> result = chatSessionService.searchMessagesByKeyword(email, keyword);
        log.info("[Chat] 키워드를 포함하는 채팅 내역 조회 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    @Operation(summary = "새로운 채팅 세션 생성", description = "정회원의 신규 채팅 세션을 생성합니다.")
    @PostMapping
    public ResponseEntity<ChatSessionResponse.SummaryDto> createChatSession(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("[Chat] 새로운 채팅 세션 생성 요청");

        ChatSessionResponse.SummaryDto result = chatSessionService.createChatSession(user.getIdentifier());
        log.info("[Chat] 새로운 채팅 세션 생성 완료 (ChatSessionId: {})", result.chatSessionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(result);
    }
}
