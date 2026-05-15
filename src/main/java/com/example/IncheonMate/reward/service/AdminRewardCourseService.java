package com.example.IncheonMate.reward.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.repository.PlaceRepository;
import com.example.IncheonMate.reward.domain.MemberReward;
import com.example.IncheonMate.reward.domain.RewardCourse;
import com.example.IncheonMate.reward.dto.AdminRewardCourseRequest;
import com.example.IncheonMate.reward.dto.AdminRewardCourseResponse;
import com.example.IncheonMate.reward.repository.RewardCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AdminRewardCourseService {

    private final RewardCourseRepository rewardCourseRepository;
    private final PlaceRepository placeRepository;

    //GET: /api/admin/reward-courses
    public List<AdminRewardCourseResponse.RewardCourseDto> retrieveRewardCourses() {
        List<RewardCourse> rewardCourses = rewardCourseRepository.findAll();
        if (rewardCourses.isEmpty()) {
            log.info("[RewardCourse] [Admin] 리워드 코스가 없음");
            return Collections.emptyList();
        }

        return rewardCourses.stream()
                .map(rewardCourse -> AdminRewardCourseResponse.RewardCourseDto.from(rewardCourse)).toList();
    }

    //POST: /api/admin/reward-courses
    @Transactional
    public AdminRewardCourseResponse.RewardCourseDto createRewardCourse(AdminRewardCourseRequest.RewardCourseCreateDto courseCreateDto) {
        // 1. 제목 중복 체크
        if (rewardCourseRepository.existsByTitle(courseCreateDto.title())) {
            log.warn("[RewardCourse] [Admin] [Create] 이미 같은 Title의 리워드 코스가 존재 (Title: {})", courseCreateDto.title());
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 같은 Title의 리워드 코스가 있습니다.\nTitle을 변경한 후 다시 시도해주세요");
        }

        // 2. 입력 데이터 검증 (naegiftIds가 비어있는지)
        List<String> naegiftIds = courseCreateDto.naegiftIds();
        if (naegiftIds == null || naegiftIds.isEmpty()) {
            log.warn("[RewardCourse] [Admin] [Create] 리워드 코스에 포함될 장소가 없음.");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "코스에 포함될 장소를 최소 1개 이상 등록해야 합니다.");
        }

        // 3. DB에서 Place 객체 한꺼번에 조회 (N+1 발생 안 함)
        Map<String, Place> placeMap = placeRepository.findAllByNaegiftIdIn(naegiftIds).stream()
                .collect(Collectors.toMap(Place::getNaegiftId, place -> place));

        // 4. Stream과 IntStream을 사용하여 순차적(spotOrder)으로 RewardSpot 리스트 생성
        List<RewardCourse.RewardSpot> newRewardSpotList = IntStream.range(0, naegiftIds.size())
                .mapToObj(index -> {
                    String naegiftId = naegiftIds.get(index);
                    Place place = placeMap.get(naegiftId);

                    if (place == null) {
                        log.warn("[RewardCourse] [Admin] [Create] Place에 없는 장소 (NaegiftId: {})", naegiftId);
                        throw new CustomException(ErrorCode.PLACE_NOT_FOUND, "등록하려는 장소를 찾을 수 없습니다. (NaegiftId: " + naegiftId + ")");
                    }

                    // 리스트의 인덱스를 기반으로 spotOrder 부여 (1부터 시작)
                    int spotOrder = index + 1;

                    // RewardSpot 엔티티를 생성하는 정적 팩토리 메서드(또는 빌더) 활용
                    return RewardCourse.RewardSpot.builder()
                            .spotOrder(spotOrder)
                            .kakaoId(place.getKakaoId())
                            .placeId(place.getId())
                            .naegiftId(place.getNaegiftId())
                            .name(place.getName())
                            .address(place.getAddress())
                            .coursePlaceCategory(place.getCoursePlaceCategory())
                            .thumbnailUrl(place.getThumbnailUrl())
                            .expertComment(place.getExpertComment())
                            .geoJsonPoint(new GeoJsonPoint(place.getX(),place.getY()))
                            .build();
                })
                .toList();

        // 5. 리워드 코스 엔티티 생성
        RewardCourse newRewardCourse = RewardCourse.builder()
                .title(courseCreateDto.title())
                .isVisible(courseCreateDto.isVisible())
                .rewardDescription(courseCreateDto.rewardDescription())
                .rewardSpots(newRewardSpotList)
                .build();

        // 6. DB 저장
        rewardCourseRepository.save(newRewardCourse);


        // 7. 생성된 엔티티를 DTO로 변환하여 반환
        return AdminRewardCourseResponse.RewardCourseDto.from(newRewardCourse);
    }

    @Transactional
    public AdminRewardCourseResponse.RewardCourseDeleteDto removeRewardCourse(String rewardCourseId) {
        // 1. 삭제할 리워드 코스 엔티티 조회
        Optional<RewardCourse> rewardCourseOpt = rewardCourseRepository.findById(rewardCourseId);

        // 2. 존재 여부 확인 및 예외 처리
        if (!rewardCourseOpt.isPresent()) {
            log.warn("[RewardCourse] [Admin] [Delete] ID에 해당하는 RewardCourse가 없습니다. (RewardCourseId: {})", rewardCourseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND, "삭제할 리워드 코스를 찾을 수 없습니다."); // ErrorCode는 프로젝트 상황에 맞게 조정
        }

        // 3. 엔티티 삭제
        rewardCourseRepository.delete(rewardCourseOpt.get());

        return AdminRewardCourseResponse.RewardCourseDeleteDto.of(rewardCourseId);
    }
}
