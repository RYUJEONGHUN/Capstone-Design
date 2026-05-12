package com.example.IncheonMate.course.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.course.domain.CuratedCourse;
import com.example.IncheonMate.course.dto.CourseResponse;
import com.example.IncheonMate.course.repository.CuratedCourseRepository;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final MemberRepository memberRepository;
    private final CuratedCourseRepository curatedCourseRepository;

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

    public CourseResponse.TravelCourseDto retrieveRecommendationCourse(String courseId) {
        Optional<CuratedCourse> curatedCourseOpt = curatedCourseRepository.findById(courseId);
        if(!curatedCourseOpt.isPresent()){
            log.warn("[Course] [Curated] 해당하는 큐레이션 코스를 찾을 수 없습니다. (CuratedCourseId: {})",courseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND);
        }

        return CourseResponse.TravelCourseDto.fromCuratedCourse(curatedCourseOpt.get());
    }
}
