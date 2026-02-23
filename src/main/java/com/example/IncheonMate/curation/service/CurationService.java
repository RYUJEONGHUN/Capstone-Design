package com.example.IncheonMate.curation.service;
import com.example.IncheonMate.curation.domain.CurationSpot;
import com.example.IncheonMate.curation.dto.CurationConfirmResponseDto;
import com.example.IncheonMate.curation.dto.CurationSpotForUserDto;
import com.example.IncheonMate.curation.repository.CurationRepository;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurationService {

    private final CurationRepository curationRepository;
    private final PlaceRepository placeRepository;
    private final CurationCacheService cacheService;
    private final MemberRepository memberRepository;
    // 쿨타임 관리를 위해 RedisTemplate 직접 사용 (StringRedisTemplate 권장)
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Redis 캐시에서 전체 리스트를 0.01초 만에 가져옴. 사용자가 24시간 내에 본 곳(Redis History)은 리스트에서 뺌.
     */
    public List<CurationSpotForUserDto> getActiveSpotsForUser(String email) {
        Member member = memberRepository.getMemberByEmail(email);
        String persona = (member != null && member.getSelectedPersonaId() != null)
                ? member.getSelectedPersonaId()
                : "1";

        List<CurationSpot> spots = cacheService.getCachedAllSpots(); // 캐시로 몽고 조회 최소화

        return spots.stream()
                .filter(spot -> !isCoolingDown(email, spot.getPlaceId()))
                .map(spot -> {
                    String comment = spot.getAiComments().getOrDefault(persona, spot.getAiComments().get("1"));

                    return CurationSpotForUserDto.builder()
                            .placeId(spot.getPlaceId())
                            .placeName(spot.getPlaceName())
                            .kakaoId(spot.getKakaoId())
                            .lat(spot.getLat())
                            .lng(spot.getLng())
                            .triggerRadius(spot.getTriggerRadius())
                            .aiComment(comment)
                            .build();
                })
                .toList();
    }

    /**
     *  앱이 "나 이거 봤어!" 보고하면 -> 24시간 쿨타임 적용
     */
    public void markAsViewed(String email, String placeId) {
        String key = "history:view:" + email + ":" + placeId;
        // Redis에 키 저장 (값은 "1", 유효기간 24시간)
        redisTemplate.opsForValue().set(key, "1", Duration.ofHours(24));
        log.info(" 유저({})가 장소({}) 팝업을 봄 -> 24시간 쿨타임 시작", email, placeId);
    }

    // 쿨타임 중인지 확인 (Redis에 키가 살아있는지 체크)
    private boolean isCoolingDown(String userId, String placeId) {
        String key = "history:view:" + userId + ":" + placeId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    /**
     * 관리자 등록 & AI 멘트 생성
     * 등록되면 전체 리스트 캐시를 날림(Evict)
     */
    @Transactional
    public void registerSpot(String placeId) {
        if (curationRepository.existsByPlaceId(placeId)) {
            throw new RuntimeException("이미 등록된 큐레이션 스팟입니다.");
        }

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다."));

        Map<String, String> generatedComments = new HashMap<>();
        generatedComments.put("1", place.getName() + "는 정말 조용해서 사색하기 좋아!");
        generatedComments.put("2", place.getName() + "의 따뜻한 메뉴가 체질에 딱이야.");
        generatedComments.put("3", place.getName() + "는 케이크가 딱이야.");
        generatedComments.put("4", place.getName() + "는 케이크가 딱이야.");

        CurationSpot spot = CurationSpot.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .lat(place.getX())
                .lng(place.getY())
                .triggerRadius(100)
                .aiComments(generatedComments)
                .isActive(true)
                .build();

        curationRepository.save(spot);

        // 저장 성공 후 캐시 제거
        cacheService.evictActiveSpotsCache();
    }

    public CurationConfirmResponseDto getConfirmDto(String email, String placeId) {
        Member member = memberRepository.getMemberByEmail(email);
        String persona = member.getSelectedPersonaId();

        CurationSpot spot = Optional.ofNullable(curationRepository.getCurationSpotByPlaceId(placeId))
                .orElseThrow(() -> new RuntimeException("큐레이션 스팟이 없습니다. placeId=" + placeId));

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다."));

        String comment = spot.getAiComments().get(persona);
        if (comment == null) comment = spot.getAiComments().get("1"); // fallback

        return CurationConfirmResponseDto.builder()
                .placeId(place.getId())
                .kakaoId(place.getKakaoId())
                .placeName(place.getName())
                .lat(spot.getLat())
                .lng(spot.getLng())
                .aiComment(comment)
                .ourRating(place.getOurRating())
                .expertComment(place.getExpertComment())
                .thumbnailUrl(place.getThumbnailUrl())
                .tags(place.getTags())
                .build();
    }
}