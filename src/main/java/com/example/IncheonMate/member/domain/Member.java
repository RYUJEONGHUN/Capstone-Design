package com.example.IncheonMate.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "members") // MongoDB의 'members' 컬렉션에 저장됨
public class Member {

    @Id // MongoDB의 _id (자동 생성되는 긴 문자열)
    private String id;

    // --- 1. 기본 인증 정보 (OAuth2/JWT) ---
    @Indexed(unique = true) // 이메일로 검색 자주 하니까 인덱스 걸기
    private String email;

    private String name; // 실명 (구글에서 줌)

    private String role; // ROLE_USER, ROLE_ADMIN

    // --- 2. 사용자 입력 프로필 [기획안 5-15 참고] ---
    private String nickname;      // 닉네임

    private String profileImage;  // 프로필 사진 URL

    private String birthDate;     // 생년월일 (YYYY-MM-DD)

    // --- 3. 여행 취향 데이터 (AI 추천 핵심) ---
    private String mbti;          // ENFP, ISTJ 등

    private String sasang;        // 사상의학 (태양인, 태음인, 소양인, 소음인)

    private String companion;     // 주 여행 동반자 (친구, 연인, 가족, 혼자)

    private String selectedPersona; // 현재 선택한 AI 페르소나 (할머니, 찐친 등)

    // --- 4. 앱 활동 데이터 ---
    @Builder.Default
    private List<String> bookmarkedPlaceIds = new ArrayList<>(); // 찜한 장소들의 ID 목록

    // --- 5. 시간 정보 (자동 관리) ---
    @CreatedDate
    private LocalDateTime createdAt; // 가입일

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정일

    // --- 편의 메서드 (데이터 수정용) ---
    public void updateProfile(String nickname, String mbti, String sasang, String companion) {
        this.nickname = nickname;
        this.mbti = mbti;
        this.sasang = sasang;
        this.companion = companion;
    }

    public void changePersona(String persona) {
        this.selectedPersona = persona;
    }
}
