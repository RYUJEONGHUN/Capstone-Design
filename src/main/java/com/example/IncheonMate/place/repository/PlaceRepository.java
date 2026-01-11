package com.example.IncheonMate.place.repository;

import com.example.IncheonMate.place.domain.Place;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

// JpaRepository -> MongoRepository<엔티티, ID타입(String)>
public interface PlaceRepository extends MongoRepository<Place, String> {

    // 1. 카카오 ID로 찾기
    Optional<Place> findByKakaoId(String kakaoId);

    // 2. 카카오 ID 리스트로 한 번에 찾기 (In 쿼리)
    // db.place.find({ kakaoId: { $in: [...] } })
    List<Place> findAllByKakaoIdIn(List<String> kakaoIds);
}