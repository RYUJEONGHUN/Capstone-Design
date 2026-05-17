package com.example.IncheonMate.course.dto;

import com.example.IncheonMate.course.domain.CuratedCourse;
import com.example.IncheonMate.member.domain.type.CoursePlaceCategory;
import com.example.IncheonMate.member.domain.Member;
import org.springframework.util.StringUtils;

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
            String naegiftUrl = StringUtils.hasText(courseSpot.getNaegiftId())
                    ? "https://shopuser-qa.naegift.com/" + courseSpot.getNaegiftId() + "?channel_no=1"
                    : null;

            return new CourseResponse.CourseSpotDto(
                    courseSpot.getSpotOrder(),
                    courseSpot.getName(),
                    courseSpot.getAddress(),
                    courseSpot.getThumbnailUrl(),
                    courseSpot.getCoursePlaceCategory(),
                    "https://place.map.kakao.com/" + courseSpot.getKakaoId(), //https://place.map.kakao.com/xxxxx
                    naegiftUrl,
                    courseSpot.getExpertComment(),
                    courseSpot.getGeoJsonPoint().getX(),
                    courseSpot.getGeoJsonPoint().getY()
            );
        }

        public static CourseResponse.CourseSpotDto fromCuratedCourseSpot(CuratedCourse.CuratedSpot curatedSpot){
            String naegiftUrl = StringUtils.hasText(curatedSpot.getNaegiftId())
                    ? "https://shopuser-qa.naegift.com/" + curatedSpot.getNaegiftId() + "?channel_no=1"
                    : null;

            return new CourseResponse.CourseSpotDto(
                    curatedSpot.getSpotOrder(),
                    curatedSpot.getName(),
                    curatedSpot.getAddress(),
                    curatedSpot.getThumbnailUrl(),
                    curatedSpot.getCoursePlaceCategory(),
                    "https://place.map.kakao.com/"+curatedSpot.getKakaoId(),
                    naegiftUrl,
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

    //썸네일 이미지를 보여주기 위해서 /images 중간에 /thumbnail을 끼워넣음(임시)
    // /images/thumbnail/xxxxx.jpg
    // /images/xxxx.jpg
    public record TravelCourseSummaryDto(
            String courseId,
            String title,
            List<String> thumbnailUrls,
            List<CoursePlaceCategory> coursePlaceCategories
    ){
        public static CourseResponse.TravelCourseSummaryDto from(CuratedCourse course){
            if(course == null) return new TravelCourseSummaryDto(null,null,Collections.emptyList(),Collections.emptyList());

            // 각 스팟의 원본 URL에서 '/images/' 부분을 '/images/thumbnail/'로 변환하여 리스트로 수집
            List<String> convertedThumbnails = course.getSpots().stream()
                    .map(CuratedCourse.CuratedSpot::getThumbnailUrl)
                    .map(url -> {
                        if (url != null && url.contains("/images/")) {
                            return url.replace("/images/", "/images/thumbnail/");
                        }
                        return url; // URL이 null이거나 패턴이 맞지 않으면 원본 반환
                    })
                    .toList();

            return new TravelCourseSummaryDto(
                    course.getId(),
                    course.getTitle(),
                    convertedThumbnails,
                    course.getSpots().stream().map(CuratedCourse.CuratedSpot::getCoursePlaceCategory).toList()
            );
        }
    }

    //reward course summary dto랑 reward course spot dto 만들어야함

}
