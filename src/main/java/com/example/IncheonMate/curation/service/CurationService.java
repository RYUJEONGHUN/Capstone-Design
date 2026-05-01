package com.example.IncheonMate.curation.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.curation.domain.CurationSpot;
import com.example.IncheonMate.curation.dto.CurationConfirmResponseDto;
import com.example.IncheonMate.curation.dto.CurationSpotForUserDto;
import com.example.IncheonMate.curation.repository.CurationRepository;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.PersonaType;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.domain.type.PlaceCategory;
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
        PersonaType persona = (member != null && member.getSelectedPersona() != null)
                ? member.getSelectedPersona()
                : PersonaType.BEAR;

        List<CurationSpot> spots = cacheService.getCachedAllSpots(); // 캐시로 몽고 조회 최소화
        log.debug("[Curation] 캐시 Spot 목록 조회 성공");

        return spots.stream()
                .filter(spot -> !isCoolingDown(email, spot.getPlaceId()))
                .map(spot -> {
                    String comment = spot.getAiComments().getOrDefault(persona, "추천 코멘트 불러오기 실패");
                    if("추천 코멘트 불러오기 실패".equals(comment)) log.warn("[Curation] 추천 코멘트 불러오기 실패");

                    return CurationSpotForUserDto.builder()
                            .placeId(spot.getPlaceId())
                            .placeName(spot.getPlaceName())
                            .kakaoId(spot.getKakaoId())
                            .x(spot.getX())
                            .y(spot.getY())
                            .triggerRadius(spot.getTriggerRadius())
                            .aiComment(comment)
                            .build();
                })
                .toList();
    }

    /**
     * 앱이 "나 이거 봤어!" 보고하면 -> 24시간 쿨타임 적용
     */
    public void markAsViewed(String email, String placeId) {
        String key = "history:view:" + email + ":" + placeId;
        // Redis에 키 저장 (값은 "1", 유효기간 24시간)
        redisTemplate.opsForValue().set(key, "1", Duration.ofHours(24));
        log.info("[Curation] 24시간 쿨타임 시작(PlaceId: {})",placeId);
    }

    // 쿨타임 중인지 확인 (Redis에 키가 살아있는지 체크)
    private boolean isCoolingDown(String userId, String placeId) {
        String key = "history:view:" + userId + ":" + placeId;
        boolean isCoolingDown = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        if(isCoolingDown) log.debug("[Curation] [CoolDown] 쿨타임 적용 중 - 조회 내역에서 제외 (PlaceId: {})",placeId);
        return isCoolingDown;
    }


    /**
     * 관리자 등록 & AI 멘트 생성
     * 등록되면 전체 리스트 캐시를 날림(Evict)
     */
    @Transactional
    public void registerSpot(String placeId) {
        if (curationRepository.existsByPlaceId(placeId)) {
            log.warn("[Curation] 이미 등록된 Curation spot (관리자용)(PlaceId: {})", placeId);
            throw new RuntimeException("이미 등록된 큐레이션 스팟입니다.");
        }

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다."));

        //카테고리에 따라서 서로 다른 멘트 4개 생성
        Map<PersonaType, String> generatedComments = generateCommentsByPlaceCategory(place.getPlaceCategory(), place.getName());

        CurationSpot spot = CurationSpot.builder()
                .placeId(place.getId())
                .placeName(place.getName())
                .y(place.getY())
                .x(place.getX())
                .triggerRadius(300)
                .aiComments(generatedComments)
                .isActive(true)
                .build();

        curationRepository.save(spot);

        // 저장 성공 후 캐시 제거
        cacheService.evictActiveSpotsCache();
        log.info("[Curation] Spot 등록 완료,캐시 제거 완료 (관리자용)(PlaceId: {})",placeId);
    }

    private Map<PersonaType, String> generateCommentsByPlaceCategory(PlaceCategory placeCategory, String placeName) {
        //AT4-관광명소, AD5-숙박, FD6-음식점, CE7-카페
        Map<PersonaType, String> generatedComments = new HashMap<>();
        switch (placeCategory) {
            case CE7:
                generatedComments.put(PersonaType.BEAR, "허허, 우리 조카가 편히 쉴 수 있는 " + placeName + " 카페에 내가 가봤는데 참 좋더라고.");
                generatedComments.put(PersonaType.FOX, "어머, 이건 진짜 나만 아는 비밀인데 특별히 알려주는 거야, " + placeName + " 커피가 아주 맛있다니까?");
                generatedComments.put(PersonaType.PANDA, "아... 졸려... 목마를 텐데 " + placeName + " 가서 음료나 마시셈.");
                generatedComments.put(PersonaType.CAT, "흥, 목마를 텐데 " + placeName + " 카페 가서 차나 마시라구.");
                break;
            case AT4:
                generatedComments.put(PersonaType.BEAR, "허허허, 우리 친구가 구경하기 알맞은 " + placeName + " 명소에 내가 예전에 가봤는데 참 좋았구먼.");
                generatedComments.put(PersonaType.FOX, "어머나, 반가워라! " + placeName + " 관광지는 진짜 나만 아는 비밀인데 특별히 알려주는 거라구!");
                generatedComments.put(PersonaType.PANDA, "음... 귀찮긴 한데... 심심하면 " + placeName + " 명소 가서 구경이나 하든가.");
                generatedComments.put(PersonaType.CAT, "야옹, 지루해 보이길래 " + placeName + " 명소를 찾아놨으니 가보든가 하냐옹?");
                break;
            case FD6:
                generatedComments.put(PersonaType.BEAR, "허허허허, 든든하게 밥 먹기 좋은 " + placeName + " 식당에 내가 가봤는데 참 좋았나 보네.");
                generatedComments.put(PersonaType.FOX, "어머머, " + placeName + " 식당은 진짜 나만 아는 비밀 맛집인데 너한테만 특별히 알려주지 뭐야!");
                generatedComments.put(PersonaType.PANDA, "하아... 피곤해... 밥은 굶지 말고 " + placeName + " 식당 가서 챙겨 먹으셈.");
                generatedComments.put(PersonaType.CAT, "흥, 참나, 밥도 안 먹고 다니는 것 같아서 " + placeName + " 식당을 찾아놨거든?");
                break;
            case AD5:
                generatedComments.put(PersonaType.BEAR, "허허 참, 우리 친구가 푹 쉴 " + placeName + " 숙소에 내가 머물러봤는데 참 좋더라고.");
                generatedComments.put(PersonaType.FOX, "어머나 세상에, 편히 쉴 수 있는 " + placeName + " 숙소는 진짜 나만 아는 비밀인데 특별히 알려줄걸?");
                generatedComments.put(PersonaType.PANDA, "어휴... 귀찮아... 밖에서 잘 수는 없으니 " + placeName + " 숙소에서 자셈.");
                generatedComments.put(PersonaType.CAT, "냐옹, 오늘 묵을 곳이 필요하면 " + placeName + " 숙소가 꽤 깔끔하다구.");
                break;
            case CT1:
                generatedComments.put(PersonaType.BEAR, "허허, 우리 조카가 여유롭게 슬슬 걸으며 이것저것 구경하기 딱 좋은 " + placeName + "에 내가 먼저 가봤는데 참 좋더라고.");
                generatedComments.put(PersonaType.FOX,"어머, 이건 진짜 나만 아는 비밀인데 특별히 알려주는 거야, " + placeName + "에 숨겨진 볼거리와 즐길 거리가 아주 가득하다니까?");
                generatedComments.put(PersonaType.PANDA,"아... 졸려... 귀찮긴 한데, 가볍게 바람 쐬면서 구경하고 돌아다니기에는 " + placeName + "도 나쁘지 않음.");
                generatedComments.put(PersonaType.CAT,"흥, 딱히 널 위해 찾은 건 아니니까 오해하지 마, 그냥 가벼운 나들이 삼아 " + placeName + "가서 구경이나 좀 하고 오라구, 냐옹.");
                break;
            default:
                log.warn("[Curation] 카테코리에 따른 코멘트 생성 불가 (관리자용)(PlaceCategory: {})", placeCategory);
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"장소 카테고리 코드가 잘못되었습니다:{"+placeCategory+"}");
        }

        return generatedComments;
    }

    public CurationConfirmResponseDto getConfirmDto(String email, String placeId) {
        Member member = memberRepository.getMemberByEmail(email);
        PersonaType persona = member.getSelectedPersona();

        CurationSpot spot = Optional.ofNullable(curationRepository.getCurationSpotByPlaceId(placeId))
                .orElseThrow(() -> new RuntimeException("큐레이션 스팟이 없습니다. placeId=" + placeId));

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다."));

        String comment = spot.getAiComments().get(persona);
        if (comment == null) comment = spot.getAiComments().get("1"); // fallback

        log.info("[Curation] Spot 상세 정보 조회 성공 (PlaceId: {})", placeId);
        return CurationConfirmResponseDto.builder()
                .placeId(place.getId())
                .kakaoId(place.getKakaoId())
                .placeName(place.getName())
                .x(spot.getX())
                .y(spot.getY())
                .aiComment(comment)
                .ourRating(place.getOurRating())
                .expertComment(place.getExpertComment())
                .thumbnailUrl(place.getThumbnailUrl())
                .tags(place.getTags())
                .build();
    }
}