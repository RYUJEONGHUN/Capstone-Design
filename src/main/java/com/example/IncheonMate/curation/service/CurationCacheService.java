package com.example.IncheonMate.curation.service;

import com.example.IncheonMate.curation.domain.CurationSpot;
import com.example.IncheonMate.curation.repository.CurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurationCacheService {

    private final CurationRepository curationRepository;

    @Cacheable(value = "curation_spots", key = "'active'")
    public List<CurationSpot> getCachedAllSpots() {
        log.info("[Cache Miss] DB에서 전체 큐레이션 스팟 조회 중...");
        return curationRepository.findAllByIsActiveTrue();
    }

    @CacheEvict(value = "curation_spots", key = "'active'")
    public void evictActiveSpotsCache() {
        // 캐시 삭제됨.
        log.info("[Cache Evict] curation_spots::active 캐시 삭제");
    }
}
