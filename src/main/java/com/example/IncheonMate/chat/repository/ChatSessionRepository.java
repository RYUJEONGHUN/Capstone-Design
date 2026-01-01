package com.example.IncheonMate.chat.repository;

import com.example.IncheonMate.chat.domain.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

    //memberId에 해당하는 전체 채팅 세션 조회
    List<ChatSession> findAllByMemberId(String memberId);

    List<ChatSession> findByMessagesContentContaining(String keyword);
}
