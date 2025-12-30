package com.example.IncheonMate.chat.repository;

import com.example.IncheonMate.chat.domain.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
}
