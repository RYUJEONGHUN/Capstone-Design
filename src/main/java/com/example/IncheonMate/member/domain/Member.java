package com.example.IncheonMate.member.domain;

import com.example.IncheonMate.member.type.CompanionType;
import com.example.IncheonMate.member.type.MbtiType;
import com.example.IncheonMate.member.type.SasangType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "members") // MongoDB의 'members' 컬렉션에 저장됨
public class Member {
    /*
    id, email, name, role, provider, createdAt, updatedAt
    lang, nickname, profileImage, profileImageAsMarker(false), birthDate
    mbti, sasang, companion, selectedPersona
     */

    @Id // MongoDB의 _id (자동 생성되는 긴 문자열)
    private String id;

    // --- 1. 기본 인증 정보 (OAuth2/JWT) ---
    @Indexed(unique = true) // 이메일로 검색 자주 하니까 인덱스 걸기
    private String email;
    private String name; // 실명
    private String role; // ROLE_USER, ROLE_ADMIN
    private String provider; //google,kakao

    // --- 2. 사용자 입력 프로필 [기획안 5-15 참고] ---
    private String lang; //kor,eng
    @Indexed(unique = true)
    private String nickname;      // 닉네임
    private String profileImage;  // 프로필 사진 URL
    @Builder.Default
    private Boolean profileImageAsMarker = false; //프로필 사진 마커로 사용할지 말지-🔺🔺
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate birthDate;     // 생년월일 (YYYY-MM-DD)

    // --- 3. 여행 취향 데이터 (AI 추천 핵심) ---
    @Field(targetType = FieldType.STRING)
    private MbtiType mbti;          // ENFP, ISTJ 등
    @Field(targetType = FieldType.STRING)
    private SasangType sasang;        // 사상의학 (태양인, 태음인, 소양인, 소음인)
    @Field(targetType = FieldType.STRING)
    private CompanionType companion;     // 주 여행 동반자 (친구, 연인, 가족, 혼자)
    private String selectedPersonaId; // 현재 선택한 AI 페르소나

    // --- 4. 앱 활동 데이터 ---
    //@Builder.Default
    //private List<String> bookmarkedPlaceIds = new ArrayList<>(); // 찜한 장소들의 ID 목록

    // --- 5. 시간 정보 (자동 관리) ---
    @CreatedDate
    private LocalDateTime createdAt; // 가입일
    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정일

    // --- 편의 메서드 (데이터 수정용) ---
    public void updateProfile(String nickname, MbtiType mbti, SasangType sasang, CompanionType companion,LocalDate birthDate) {
        this.nickname = nickname;
        this.mbti = mbti;
        this.sasang = sasang;
        this.companion = companion;
        this.birthDate = birthDate;
    }


}
