package com.example.IncheonMate.member.repository;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
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

    //findByEmail의 orElseThrow() 중복을 피하기 위해서 작성
    default Member findByEmailOrElseThrow(String email){
        return findByEmail(email)
                .orElseThrow(() -> {
                    //인터페이스에서는 @Slf4j를 못쓰니 직접 호출
                    Logger log = LoggerFactory.getLogger(MemberRepository.class);
                    log.warn("{}에 해당하는 멤버를 찾을 수 없습니다.",email);

                    return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
                });
    }

    //최근 길찾기 기록을 제거
    @Query("{ 'email' : ?0 }")
    @Update("{ '$pull' : { 'recentRoutes' : { '_id' : ?1 } } }")
    int deleteRecentRouteByEmail(String email, String routeId);

    //최근 검색기록을 제거
    @Query("{ 'email' : ?0 }")
    @Update("{ '$pull' : { 'recentSearches' : { '_id' : ?1 } } }")
    int deleteRecentSearchByEmail(String email, String recentSearchId);

    Member getMemberByEmail(String email);
}
