package com.example.IncheonMate.chat.domain;

import com.example.IncheonMate.chat.domain.type.AuthorType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chatSessions")
@Getter
@NoArgsConstructor
public class ChatSession {

    //---세션 기본 정보
    @Id
    private String id;
    //private String title; //MyInfo에 보여줄떄 내용 요약(선택)
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;//수동으로 업데이트 해야함-메시지 업데이트할 때만 바뀌어야 하기 때문에

    //@DBRef를 이용해서 관계 설정할 수 있지만 나중에 Lazy loading설정이 어렵고 N+1문제 발생 가능성이 높아서 수동 추천
    private String memberId; //member Entity의 @Id값


    //---메시지 내역
    private List<Message> messages = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message{
        //메시지 기본 정보
        //MongoDB는 메시지가 추가될 때마다 전 세계 모든 메시지 ID를 뒤져서 중복을 검사해야 하므로 채팅 속도가 느려질 수 있습니다.
        private String id; //내장 doc는 자동으로 Id 생성이 안됨 => 직접 UUID 생성
        @CreatedDate
        private LocalDateTime createdAt;
        private AuthorType authorType; //USER,AI enum

        //메시지 내용
        private String content;
        //private ???? messageVector; //AI용 메시지 벡터
    }
}
