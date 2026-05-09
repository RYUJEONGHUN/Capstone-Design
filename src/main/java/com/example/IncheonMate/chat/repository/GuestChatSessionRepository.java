package com.example.IncheonMate.chat.repository;

import com.example.IncheonMate.chat.domain.GuestChatSession;
import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestChatSessionRepository extends CrudRepository<GuestChatSession, String> {

    default GuestChatSession findByIdOrElseThrow(String guestId){
        return findById(guestId).orElseThrow(() -> {
                Logger log = LoggerFactory.getLogger(GuestChatSessionRepository.class);
                log.warn("[Chat] 게스트를 찾을 수 없음");

                return new CustomException(ErrorCode.MEMBER_NOT_FOUND, "게스트를 찾을 수 없습니다.");
        });
    }
}
