package com.example.IncheonMate.member.domain;

import com.example.IncheonMate.member.domain.type.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import com.mongodb.lang.Nullable;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.aggregation.VectorSearchOperation;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"recentSearches", "recentRoutes", "favoritePlaces"})
@Document(collection = "members") // MongoDB의 'members' 컬렉션에 저장됨
public class Member {
    @Field(targetType = FieldType.STRING)
    private CompanionType companion;


    @Id // MongoDB의 _id (자동 생성되는 긴 문자열)
    private String id;

    // --- 1. 기본 인증 정보 (OAuth2/JWT) ---
    @Indexed(unique = true) // 이메일로 검색 자주 하니까 인덱스 걸기
    private String email;
    private String name; // 실명
    private String role; // ROLE_USER, ROLE_ADMIN,ROLE_GUEST
    private String provider; //google,kakao

    // --- 추가. 사용자 약관
    //동의 여부
    private boolean isPrivacyPolicyAgreed; //개인정보 처리방침 동의
    private boolean isLocationServiceAgreed; //위치기반 서비스 동의
    private boolean isTermsOfServiceAgreed; //개인정보 동의
    //동의 시점-@CreatedDate 사용하면 안됨
    private LocalDateTime allTermsAgreedAt; //필수 약관 3개에 동의한 시간
    //약관 버전
    private String termsVersion; //약관 버전 관리

    // --- 2. 사용자 입력 프로필 [기획안 5-15 참고] ---
    private String lang; //kor,eng
    @Indexed(unique = true, sparse = true)
    private String nickname;  // 닉네임
    @Nullable
    private String profileImageURL;  // 프로필 사진 URL
    private boolean profileImageAsMarker; //프로필 사진 마커로 사용할지 말지
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthDate;     // 생년월일 (YYYY-MM-DD)
    private Gender gender;

    // --- 3. 여행 취향 데이터 (AI 추천 핵심) ---
    @Field(targetType = FieldType.STRING)
    private MbtiType mbti;          // ENFP, ISTJ 등,Enum
    @Field(targetType = FieldType.STRING)
    private SasangType sasang;        // 사상의학 (태양인, 태음인, 소양인, 소음인), Enum
    private PersonaType selectedPersona; // 현재 선택한 AI 페르소나ID

    // --- 4. 시간 정보 (자동 관리) ---
    @CreatedDate
    private LocalDateTime createdAt; // 가입일
    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정일

    // --- 추가 내기프트 리워드 정보 ---
    @Builder.Default
    private List<String> naegiftCoupons = new ArrayList<>();

    // --- 5. 찜 목록 ---
    @Builder.Default // 빌더 사용 시 null 방지
    private List<FavoritePlace> favoritePlaces = new ArrayList<>(); // 찜한 장소들(FavoritePlace의 List형태)

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FavoritePlace{
        // 내장 객체의 ID에는 @Indexed(unique=true)를 절대 걸면 안 됨! (UUID 생성 후 저장)
        private String id; //수동 UUID
        private LocalDateTime createdAt;
        private String kakaoPlaceId; //카카오에서 제공해주는 장소 Id값
        private String placeName; //장소 이름
        private GeoJsonPoint location; //좌표
        private String address; //주소

        private boolean isRegistered;
        @Nullable private Double ourRating; //우리가 크롤링한 평점
        // -> 추후 서비스내에서 자체 평점 시스템 만들어야함
    }


    //--- 7. 길찾기 탭의 검색어 기록(최대 개수 일단 20개로 만들고 나중에 수정)
    @Builder.Default // 빌더 사용 시 null 방지
    private List<RecentSearch> recentSearches = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentSearch{
        //키워드 저장할떄 사용
        private String id; //수동 UUID
        private String keyword; //검색어 또는 장소명
        private LocalDateTime searchedAt; //검색한 시간
    }

    //--- 8. 길찾기 탭의 경로 검색 기록(최대 개수 일단 20개로 만들고 나중에 수정)
    @Builder.Default // 빌더 사용 시 null 방지
    private List<RecentRoute> recentRoutes = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentRoute{
        private String id; //수동 UUID
        private String departureName; //출발 장소 이름
        private String arrivalName; //도착 장소 이름
        private GeoJsonPoint departureLocation; //출발 장소 좌표
        private GeoJsonPoint arrivalLocation;//도착 장소 좌표
        private LocalDateTime searchedAt; //검색한 시간
    }

    //AI 생성 코스는 1개만 가능
    private TravelCourse travelCourse;


    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TravelCourse{
        private String id;
        private String title; //[사용자명]님을 위한 맞춤 여행 코스
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Builder.Default
        private List<CourseSpot> spots = new ArrayList<>();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CourseSpot{
        private int spotOrder;
        private String kakaoId;
        @Nullable private String naegiftId;
        private String name;
        private String address;
        private CoursePlaceCategory coursePlaceCategory;
        private String thumbnailUrl;
        private String expertComment;
        private GeoJsonPoint geoJsonPoint;
    }

    public void deliverNaegiftCoupontoMember(String couponId){
        this.getNaegiftCoupons().add(couponId);
    }
}
