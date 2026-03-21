package com.example.IncheonMate.chat.controller;

import com.example.IncheonMate.chat.dto.ChatResponse;
import com.example.IncheonMate.chat.service.ChatService;
import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(summary = "오늘 채팅 내역 불러오기", description = "메인 화면에서 채팅 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "채팅 내역 전체 조회 성공", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChatResponse.TodayDto.class)))),
            @ApiResponse(responseCode = "404", description = "채팅 세션을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/today")
    public ResponseEntity<ChatResponse.TodayDto> getTodayChat(@AuthenticationPrincipal CustomOAuth2User user){
        String identifier = user.getIdentifier();
        log.info("오늘 채팅 내역 조회 요청:{}",identifier);

        return ResponseEntity.status(HttpStatus.OK)
                .body(chatService.getTodayChat(identifier, user.isGuest()));
    }

}
