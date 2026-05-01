package com.example.IncheonMate.member.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.dto.MemberChatProfileDto;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberChatProfileService {
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    public MemberChatProfileDto.ProfileResponse getProfile(String identifier){
        //0. 이메일 형식('@')인 경우에만 DB 조회
        if(identifier.contains("@")) {
            // 1. DB에서 정회원인지 먼저 조회
            Optional<Member> member = memberRepository.findByEmail(identifier);

            // 2. 정회원이면 회원 데이터를 반환하고 메서드 종료
            if (member.isPresent()) {
                log.debug("[Member] 정회원 프로필 조회 성공");
                return MemberChatProfileDto.ProfileResponse.fromMember(member.get());
            }
        }

        // 3. DB에 없다면 에러를 던지지 않고, 게스트인지 확인하기 위해 Redis 조회 진행
        log.debug("[Membmer] 정회원이 아님. 게스트 프로필 조회를 시도");
        Map<Object,Object> guestProfile = redisTemplate.opsForHash().entries("GUEST_PROFILE:" + identifier);

        // 4. Redis에도 없다면, 그때서야 진짜 예외(에러) 발생
        if(guestProfile.isEmpty()){
            log.warn("[Member] 정회원 및 게스트 정보 조회 실패");
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND, "회원 및 게스트 정보를 찾을 수 없습니다.");
        }

        // 5. 게스트 데이터가 존재하면 반환
        log.info("[Member] 게스트 프로필 조회 성공");
        return MemberChatProfileDto.ProfileResponse.fromGuest(identifier, guestProfile);
    }
}
