package com.example.IncheonMate.chat.dto;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public class ChatSessionResponse {

    //채팅 세션 기록:채팅 세션의 압축된 정보만 보여줌
    @Schema(description = "채팅 세션 요약")
    public record SummaryDto( //class
                              @Schema(description = "채팅 세션 ID", example = "65a1b2c....")
                              String chatSessionId,
                              @Schema(description = "채팅 세션 생성 시간", example = "2026-01-14T08:37:59.560Z")
                              LocalDateTime createdAt,
                              @Schema(description = "마지막 채팅 시간", example = "2026-01-14T08:37:59.560Z")
                              LocalDateTime lastMessagedAt
                              //String title; //나중에 AI로 제목 요약을 할 수 있으면 추가
    ) {
        public static SummaryDto from(ChatSession chatSession) { //static method
            return new SummaryDto(
                    chatSession.getId(),
                    chatSession.getCreatedAt(),
                    chatSession.getLastMessageAt()
            );
        }
    }

    //세부 채팅 기록: 선택한 채팅 세션에 해당하는 채팅 전체를 보내줌
    @Schema(description = "채팅 세션 상세 내역 (전체 메시지 포함)")
    public record DetailDto(
            @Schema(description = "채팅 세션 ID", example = "65a1b2c....")
            String chatSessionId,
            @ArraySchema(schema = @Schema(description = "채팅 메시지", implementation = ChatSessionResponse.MessageDto.class))
            List<MessageDto> messages
    ) {
        public static DetailDto from(ChatSession chatSession) {
            List<MessageDto> messageDtos = chatSession.getMessages().stream()
                    .map(MessageDto::from)
                    .toList();

            return new DetailDto(
                    chatSession.getId(),
                    messageDtos
            );
        }
    }

    //키워드 검색: 전체 채팅 세션에서 검색한 결과를 보내줌
    @Schema(description = "키워드 검색 메시지 DTO")
    public record SearchedMessageDto(
            @Schema(description = "메시지 ID(UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            String messageId,
            @Schema(description = "메시지 생성 일시", example = "2026-01-14T08:37:59.560Z")
            LocalDateTime createdAt,
            @Schema(description = "메시지 작성자 구분 (USER: 사용자, AI: 챗봇)", example = "USER", implementation = AuthorType.class)
            AuthorType authorType,
            @Schema(description = "메시지 내용", example = "근처에 맛집 추천해줘")
            String content
    ) {
        public static SearchedMessageDto from(ChatSession.Message message) {
            return new SearchedMessageDto(
                    message.getId(),
                    message.getCreatedAt(),
                    message.getAuthorType(),
                    message.getContent()
            );
        }
    }

    //유지보수용 messagedto분리
    @Schema(description = "개별 채팅 메시지 데이터")
    public record MessageDto(
            @Schema(description = "메시지 ID(UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            String messageId,
            @Schema(description = "메시지 생성 일시", example = "2026-01-14T08:37:59.560Z")
            LocalDateTime createdAt,
            @Schema(description = "메시지 작성자 구분 (USER: 사용자, AI: 챗봇)", example = "USER", implementation = AuthorType.class)
            AuthorType authorType,
            @Schema(description = "메시지 내용", example = "근처에 맛집 추천해줘")
            String content
    ) {
        public static MessageDto from(ChatSession.Message message) {
            return new MessageDto(
                    message.getId(),
                    message.getCreatedAt(),
                    message.getAuthorType(),
                    message.getContent()
            );
        }
    }
}
