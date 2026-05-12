package com.example.IncheonMate.course.dto;

import com.example.IncheonMate.course.domain.CuratedCourse;
import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.example.IncheonMate.member.domain.Member;

import java.util.Collections;
import java.util.List;

public class CourseResponse {

    public record CourseSpotDto(
            int spotOrder, //여행 코스 순서
            String name, // 장소명
            String address, //주소
            String thumbnailUrl, //사진
            CoursePlaceCategory coursePlaceCategory, //카테고리(
            String kakaoUrl,
            String naegiftUrl, //내기프트 URL
            String expertComment,
            Double x,
            Double y
    ) {
        public static CourseResponse.CourseSpotDto fromMemberCourseSpot(Member.CourseSpot courseSpot){
            return new CourseResponse.CourseSpotDto(
                    courseSpot.getSpotOrder(),
                    courseSpot.getName(),
                    courseSpot.getAddress(),
                    courseSpot.getThumbnailUrl(),
                    courseSpot.getCoursePlaceCategory(),
                    "https://place.map.kakao.com/" + courseSpot.getKakaoId(), //https://place.map.kakao.com/xxxxx
                    courseSpot.getNaegiftId() != null ? "https://shopuser-qa.naegift.com/" + courseSpot.getNaegiftId() + "?channel_no=1" : null, //https://shopuser-qa.naegift.com/xxxxx?channel_no=1
                    courseSpot.getExpertComment(),
                    courseSpot.getGeoJsonPoint().getX(),
                    courseSpot.getGeoJsonPoint().getY()
            );
        }

        public static CourseResponse.CourseSpotDto fromCuratedCourseSpot(CuratedCourse.CuratedSpot curatedSpot){
            return new CourseResponse.CourseSpotDto(
                    curatedSpot.getSpotOrder(),
                    curatedSpot.getName(),
                    curatedSpot.getAddress(),
                    curatedSpot.getThumbnailUrl(),
                    curatedSpot.getCoursePlaceCategory(),
                    "https://place.map.kakao.com/"+curatedSpot.getKakaoId(),
                    curatedSpot.getNaegiftId() != null ? "https://shopuser-qa.naegift.com/" + curatedSpot.getNaegiftId() + "?channel_no=1" : null,
                    curatedSpot .getExpertComment(),
                    curatedSpot.getGeoJsonPoint().getX(),
                    curatedSpot.getGeoJsonPoint().getY()
            );
        }
    }

    public record TravelCourseDto(
            String courseId,
            String title,
            List<CourseResponse.CourseSpotDto> courseSpots
    ) {
        public static CourseResponse.TravelCourseDto fromMember(Member member){
            Member.TravelCourse travelCourse = member.getTravelCourse();

            if(travelCourse == null || travelCourse.getId() == null){
                return new CourseResponse.TravelCourseDto(null,null, Collections.emptyList());
            }

            return new CourseResponse.TravelCourseDto(
                    travelCourse.getId(),
                    travelCourse.getTitle(),
                    travelCourse.getSpots().stream()
                            .map(CourseResponse.CourseSpotDto::fromMemberCourseSpot)
                            .toList()
            );
        }

        public static CourseResponse.TravelCourseDto fromCuratedCourse(CuratedCourse curatedCourse){
            return new CourseResponse.TravelCourseDto(
                    curatedCourse.getId(),
                    curatedCourse.getTitle(),
                    curatedCourse.getSpots().stream()
                            .map(CourseResponse.CourseSpotDto::fromCuratedCourseSpot)
                            .toList()
            );
        }
    }

    public record TravelCourseSummaryDto(
            String courseId,
            String title,
            List<String> thumbnailUrls,
            List<CoursePlaceCategory> coursePlaceCategories
    ){
        public static CourseResponse.TravelCourseSummaryDto from(CuratedCourse course){
            return new TravelCourseSummaryDto(
                    course.getId(),
                    course.getTitle(),
                    course.getSpots().stream().map(CuratedCourse.CuratedSpot::getThumbnailUrl).toList(),
                    course.getSpots().stream().map(CuratedCourse.CuratedSpot::getCoursePlaceCategory).toList()
            );
        }
    }

    //reward course summary dto랑 reward course spot dto 만들어야함

}
