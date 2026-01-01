package com.example.IncheonMate.member.repository;

import com.example.IncheonMate.member.domain.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

// <Entity 타입, ID 타입>
public interface MemberRepository extends MongoRepository<Member, String> {

    // 이메일로 회원 찾기 (로그인할 때 필수)
    Optional<Member> findByEmail(String email);

    // 닉네임 중복 검사할 때 사용
    boolean existsByNickname(String nickname);

    // 이메일 존재 여부 확인 (JWT 검증 시 가끔 사용)
    boolean existsByEmail(String email);

    @Query(value = "{ 'email': ?0 }", fields = "{ 'favoritePlaces' : 1, '_id': 0 }")
    Optional<Member> findFavoritePlacesByEmail(String email);
}
