package com.example.IncheonMate.chat.controller;

import com.example.IncheonMate.chat.dto.ChatRequest;
import com.example.IncheonMate.chat.dto.ChatResponse;
import com.example.IncheonMate.chat.service.ChatService;
import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
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

import java.time.temporal.ChronoUnit;

//채팅창 내부에서 일어나는 "실시간 상호작용" 기능.
@Tag(name = "Chat API", description = "채팅창 내부에서 일어나는 \"실시간 상호작용\" 기능.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    //메인 화면에서 채팅을 내역을 불러올 때 사용
    @Operation(summary = "최신 채팅 내역 불러오기", description = "메인 화면에서 채팅 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 내역 전체 조회 성공", content = @Content(schema = @Schema(implementation = ChatResponse.CurrentDto.class))),
            @ApiResponse(responseCode = "404", description = "채팅 세션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/current")
    public ResponseEntity<ChatResponse.CurrentDto> getCurrentChat(@AuthenticationPrincipal CustomOAuth2User user){
        String identifier = user.getIdentifier();
        log.info("[Chat] 최근 채팅 내역 조회 요청");

        ChatResponse.CurrentDto result = chatService.getCurrentChat(identifier, user.isGuest());
        log.info("[Chat] 최근 채팅 내역 조회 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

    //채팅창에서 채팅 입력
    @Operation(summary = "채팅 전송", description = "채팅 입력 후 AI에게 메시지를 전송합니다.")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "채팅 성공", content = @Content(schema = @Schema(implementation = ChatResponse.Generation.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 채팅 요청", content = @Content(schema = @Schema(implementation = CustomException.class))),
            @ApiResponse(responseCode = "500", description = "AI 서버 응답 지연 또는 시스템 오류", content = @Content(schema = @Schema(implementation = CustomException.class)))
    })
    @PostMapping
    public ResponseEntity<ChatResponse.Generation> sendChat(@AuthenticationPrincipal CustomOAuth2User user, @RequestBody ChatRequest.MessageDto messageDto){
        String identifier = user.getIdentifier();
        log.info("[Chat] AI 채팅 요청");

        ChatResponse.Generation result = chatService.sendChatMessage(identifier, user.isGuest(), messageDto);
        log.info("[Chat] AI 채팅 응답 성공");
        return ResponseEntity.status(HttpStatus.OK)
                .body(result);
    }

}
