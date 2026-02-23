package com.example.IncheonMate.place.repository;

import com.example.IncheonMate.place.domain.Place;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

// JpaRepository -> MongoRepository<엔티티, ID타입(String)>
public interface PlaceRepository extends MongoRepository<Place, String> {

    // 1. 카카오 ID로 찾기
    Optional<Place> findByKakaoId(String kakaoId);

    // 2. 카카오 ID 리스트로 한 번에 찾기 (In 쿼리)
    // db.place.find({ kakaoId: { $in: [...] } })
    List<Place> findAllByKakaoIdIn(List<String> kakaoIds);

    Place getPlaceById(String id);

    // 1. 첫 번째 메서드: AI 의도 검색
    @Query("{ 'address': { '$regex': ?0, '$options': 'i' }, " +
            "'categoryGroup': ?1, " +
            "'$or': [ { 'tags': ?2 }, { 'tags': ?3 } ] }")
    List<Place> findByAiIntent(String location, String category, String vibe, String companion); // 세미콜론은 여기서 딱 한 번!

    // 2. 두 번째 메서드: 기본 지역+카테고리 검색
    @Query("{ 'address': { '$regex': ?0, '$options': 'i' }, 'categoryGroup': ?1 }")
    List<Place> findByAddressContainingAndCategoryGroup(String location, String category); // 여기도 끝에만 딱!

}