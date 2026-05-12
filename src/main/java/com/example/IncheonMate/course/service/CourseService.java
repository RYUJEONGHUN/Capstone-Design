package com.example.IncheonMate.course.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.course.domain.CuratedCourse;
import com.example.IncheonMate.course.dto.CourseRequest;
import com.example.IncheonMate.course.dto.CourseResponse;
import com.example.IncheonMate.course.repository.CuratedCourseRepository;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final MemberRepository memberRepository;
    private final CuratedCourseRepository curatedCourseRepository;
    private final PlaceRepository placeRepository;

    //GET: /current
    public CourseResponse.TravelCourseDto retrieveCurrentCourse(String identifier) {
        Member member = memberRepository.findByEmailOrElseThrow(identifier);

        return CourseResponse.TravelCourseDto.fromMember(member);
    }

    //GET: /recommendations
    public List<CourseResponse.TravelCourseSummaryDto> retrieveRecommendationCourses() {
        List<CuratedCourse> curatedCourseList = curatedCourseRepository.findByIsVisibleTrue();
        if(curatedCourseList.isEmpty()){
            log.warn("[Course] [Curated] 보여줄 큐레이션 코스 목록이 없습니다. (VisibleCuatedCourseSize: 0)");
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND, "보여줄 큐레이션 코스 목록이 없습니다");
        }

        return curatedCourseList.stream()
                .map(CourseResponse.TravelCourseSummaryDto::from)
                .toList();
    }

    // GET: /recommendations/{course-id}
    public CourseResponse.TravelCourseDto retrieveRecommendationCourse(String courseId) {
        Optional<CuratedCourse> curatedCourseOpt = curatedCourseRepository.findById(courseId);
        if(!curatedCourseOpt.isPresent()){
            log.warn("[Course] [Curated] 해당하는 큐레이션 코스를 찾을 수 없습니다. (CuratedCourseId: {})",courseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        return CourseResponse.TravelCourseDto.fromCuratedCourse(curatedCourseOpt.get());
    }

    //DELETE: /recommendations/{course-id}
    @Transactional
    public void removeCuratedCourse(String curatedCourseId) {
        Optional<CuratedCourse> curatedCourseOpt = curatedCourseRepository.findById(curatedCourseId);
        if(!curatedCourseOpt.isPresent()){
            log.warn("[Course] [Recommendation] [Delete] ID에 해당하는 CuratedCourse가 없습니다. (CuratedCourseId: {})",curatedCourseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        curatedCourseRepository.delete(curatedCourseOpt.get());
    }

    //POST: /recommendations
    @Transactional
    public CourseResponse.TravelCourseDto registerCuratedCourse(CourseRequest.CreateCuratedCourse curatedCourse) {
        //1. 제목 중복 체크
        if(curatedCourseRepository.existsByTitle(curatedCourse.title())){
            log.warn("[Course] [Recommendation] [Create] 이미 같은 Title의 추천 코스가 존재 (Title: {})",curatedCourse.title());
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 같은 Title의 여행 코스가 있습니다.\nTitle을 변경하거나 여행 코스를 삭제한후 다시 시도해주세요");
        }

        // 2. 입력 데이터 무결성 검사 (1부터 순차적인지)
        if (!isValidSpotOrder(curatedCourse.createCuratedSpotList())) {
            log.warn("[Course] [Recommendation] [Create] 장소 순서가 올바르지 않음.");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "장소 순서가 올바르지 않습니다. (1부터 순차적이어야 함)");
        }

        //3. 추천 코스 엔티티 생성
        //3.1 DTO에서 placeId만 추출
        List<String> placeIdList = curatedCourse.createCuratedSpotList().stream()
                .map(CourseRequest.CreateCuratedSpot::placeId)
                .toList();

        //3.2 DB에서 Place 객체 한꺼번에 조회(N+1 발생 안함)
        Map<String, Place> placeMap = placeRepository.findAllById(placeIdList).stream()
                .collect(Collectors.toMap(Place::getId, place -> place));

        //3.3. Stream을 사용하여 CuratetdSpot 리스트 생성
        List<CuratedCourse.CuratedSpot> newCuratedSpotList = curatedCourse.createCuratedSpotList().stream()
                .map(spotDto -> {
                    Place place = placeMap.get(spotDto.placeId());

                    if(place == null) {
                        log.warn("[Course] [Recommendation] [Create] Place에 없는 장소 (PlaceId: {})", spotDto.placeId());
                        throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
                    }
                        return CuratedCourse.CuratedSpot.of(place,spotDto.spotOrder());

                })
                .toList();

        //3.4 추천 코스 엔티티 생성
        CuratedCourse newCuratedCourse = CuratedCourse.builder()
                .title(curatedCourse.title())
                .isVisible(curatedCourse.isVisible())
                .spots(newCuratedSpotList)
                .build();

        curatedCourseRepository.save(newCuratedCourse);

        return CourseResponse.TravelCourseDto.fromCuratedCourse(newCuratedCourse);
    }

    private boolean isValidSpotOrder(List<CourseRequest.CreateCuratedSpot> curatedSpotList) {
        List<Integer> orders = curatedSpotList.stream()
                .map(CourseRequest.CreateCuratedSpot::spotOrder)
                .sorted()
                .toList();

        // IntStream.range를 활용하여 1부터 N까지의 숫자와 일치하는지 한 번에 확인
        return IntStream.range(0, orders.size())
                .allMatch(i -> orders.get(i) == i + 1);
    }

}
