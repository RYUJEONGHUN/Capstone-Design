package com.example.IncheonMate.chat.domain;

import com.example.IncheonMate.chat.domain.type.AuthorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash(value = "GUEST_CHAT", timeToLive = 1209600)//TTL 14일
public class GuestChatSession implements Serializable {

    @Id
    private String id;//`GUEST_CHAT:GUEST_UUID`로 Redis Key를 만들어야 하기 때문에 이 Entity의 id를 guest의 UUID로 해야한다.

    private String title;//"Guest"+identifier.substring(0,4)+"-Chat"
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message implements Serializable{

        private String id;
        private LocalDateTime messagedAt;
        private AuthorType authorType;
        private String content;
    }


    public void addMessages(Message userMessage,Message aiMessage){
        this.messages.add(userMessage);
        this.messages.add(aiMessage);
        this.lastMessageAt = LocalDateTime.now();
    }

}
