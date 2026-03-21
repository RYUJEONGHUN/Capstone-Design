package com.example.IncheonMate.chat.repository;

import com.example.IncheonMate.chat.domain.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {

    //memberId에 해당하는 전체 채팅 세션 조회
    List<ChatSession> findAllByMemberId(String memberId);

    //채팅 키워드 검색할 때 사용(searchMessagesByKeyword)
    List<ChatSession> findByMemberIdAndMessagesContentContaining(String id, String keyword);

    //오늘 채팅한 기록을 찾을때
    // memberId가 일치하고, 생성일과 마지막 메시지일이 start(오늘 00시)와 end(오늘 23시59분) 사이인 세션 조회
    @Query("{ 'memberId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    Optional<ChatSession> findTodaySessionByMemberId(String memberid, LocalDateTime start, LocalDateTime end);

}
