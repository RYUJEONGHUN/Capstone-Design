package com.example.IncheonMate.curation.repository;


import com.example.IncheonMate.curation.domain.CurationSpot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CurationRepository extends MongoRepository<CurationSpot, String> {
    // 활성화된 스팟만 가져오기
    List<CurationSpot> findAllByIsActiveTrue();
    // 중복 등록 방지용
    boolean existsByPlaceId(String placeId);

    CurationSpot getCurationSpotByPlaceId(String placeId);
}