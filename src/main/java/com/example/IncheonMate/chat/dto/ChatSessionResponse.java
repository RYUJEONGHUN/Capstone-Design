package com.example.IncheonMate.chat.dto;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;

import java.time.LocalDateTime;
import java.util.List;

public class ChatSessionResponse {

    //채팅 기록:어떤 채팅을 했는지 날짜와 제목만 보내줌
    public record SummaryDto( //class
            String chatSessionId,
            LocalDateTime createdAt,
            LocalDateTime lastMessagedAt
            //String title; //나중에 AI로 제목 요약을 할 수 있으면 추가
    ){
        public static SummaryDto from(ChatSession chatSession){ //static method
            return new SummaryDto(
                    chatSession.getId(),
                    chatSession.getCreatedAt(),
                    chatSession.getLastMessageAt()
            );
        }
    }

    //세부 채팅 기록: 선택한 채팅 세션에 해당하는 채팅 전체를 보내줌
    public record DetailDto(
            String chatSessionId,
            List<MessageDto> messages
    ){
        public static DetailDto from(ChatSession chatSession){
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
    public record SearchedMessageDto(
             String messageId,
             LocalDateTime createdAt,
             AuthorType authorType,
             String content
    ){
        public static SearchedMessageDto from(ChatSession.Message messages){
            return new SearchedMessageDto(
                    messages.getId(),
                    messages.getCreatedAt(),
                    messages.getAuthorType(),
                    messages.getContent()
            );
        }
    }

    //유지보수용 messagedto분리
    public record MessageDto(
            String messageId,
            LocalDateTime createdAt,
            AuthorType authorType,
            String content
    ){
        public static MessageDto from(ChatSession.Message message){
            return new MessageDto(
                    message.getId(),
                    message.getCreatedAt(),
                    message.getAuthorType(),
                    message.getContent()
            );
        }
    }
}
