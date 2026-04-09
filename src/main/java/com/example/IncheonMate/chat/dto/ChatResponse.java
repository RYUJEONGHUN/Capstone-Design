package com.example.IncheonMate.chat.dto;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;

import java.time.LocalDateTime;
import java.util.List;

public class ChatResponse {

    public record TodayDto(
            String sessionId, //세션 ID
            String title, //제목
            List<MessageDto> messages,
            Integer remainingChatCount //게스트 남은 채팅 횟수(null이면 정회원,not null이면 게스트): 프론트엔드는 이걸로 정회원,게스트 분리
    ){
        public static ChatResponse.TodayDto of(String sessionId,String title,List<MessageDto> messages, Integer remainingChatCount){
            return new ChatResponse.TodayDto(sessionId,title,messages,remainingChatCount);
        }

        public static ChatResponse.TodayDto fromMember(ChatSession chatSession){
            List<MessageDto> messageDtoList = chatSession.getMessages().stream()
                    .map(ChatResponse.MessageDto::fromMember)
                    .toList();
            return new ChatResponse.TodayDto(chatSession.getId(), chatSession.getTitle().toString(),messageDtoList,null);
        }
    }

    public record MessageDto(
            String messageId,
            LocalDateTime messagedAt,
            AuthorType authorType,
            String content
    ){
        public static ChatResponse.MessageDto fromMember(ChatSession.Message message){
            return new ChatResponse.MessageDto(message.getId(), message.getMessagedAt(),message.getAuthorType(),message.getContent());
        }

        public static ChatResponse.MessageDto fromGuest(GuestChatSession.Message message){
            return new ChatResponse.MessageDto(message.getId(), message.getMessagedAt(),message.getAuthorType(),message.getContent());
        }
    }

    public record Generation(
            MessageDto user,
            MessageDto ai
    ){
        public static ChatResponse.Generation fromGuest(GuestChatSession.Message userMessage, GuestChatSession.Message aiChatMessage){
            return new Generation(
                    MessageDto.fromGuest(userMessage),
                    MessageDto.fromGuest(aiChatMessage)
            );
        }

        public static ChatResponse.Generation fromUser(ChatSession.Message userMessage, ChatSession.Message aiMessage){
            return new Generation(
                    MessageDto.fromMember(userMessage),
                    MessageDto.fromMember(aiMessage)
            );
        }
    }
}
