package com.example.IncheonMate.common.config;

import com.example.IncheonMate.chat.domain.ChatSession;
import com.example.IncheonMate.chat.domain.type.AuthorType;
import com.example.IncheonMate.chat.repository.ChatSessionRepository;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.repository.MemberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// 백엔드 테스트를 위한 개발용 임시 데이터
@Configuration
public class TestDataInitializer {


    // 2. 기존 멤버 초기화 (유지)
    @Bean
    CommandLineRunner initMemberData(MemberRepository memberRepository) {
        return args -> {

            String myEmail = "246813579246813579q@gmail.com";

            memberRepository.findByEmail(myEmail).ifPresent(member -> {
                if (member.getFavoritePlaces() == null || member.getFavoritePlaces().isEmpty()) {
                    Member.FavoritePlace place1 = Member.FavoritePlace.builder()
                            .id(UUID.randomUUID().toString())
                            .kakaoPlaceId("ChIJN1t_tDeuEmsRUsoyG83frY4")
                            .placeName("인천 센트럴파크")
                            .location(new GeoJsonPoint(126.6392, 37.3928))
                            .rating(4.8f)
                            .createdAt(LocalDateTime.now())
                            .kakaoMapUrl("https://maps.google.com/?q=Incheon+Central+Park")
                            .build();

                    Member.FavoritePlace place2 = Member.FavoritePlace.builder()
                            .id(UUID.randomUUID().toString())
                            .kakaoPlaceId("ChIJQw7FbEeuejgR9S_0v...dummy")
                            .placeName("차이나타운")
                            .location(new GeoJsonPoint(126.6181, 37.4754))
                            .rating(4.2f)
                            .createdAt(LocalDateTime.now().minusDays(2))
                            .kakaoPlaceId("https://maps.google.com/?q=Chinatown")
                            .build();

                    Member updatedMember = member.toBuilder()
                            .favoritePlaces(List.of(place1, place2))
                            .build();

                    memberRepository.save(updatedMember);
                    System.out.println("✅ 기존 멤버(" + myEmail + ")에 찜 목록 더미 데이터가 추가되었습니다.");
                }
            });
        };
    }

    // ⭐ 3. [추가] 채팅 세션 및 메시지 더미 데이터 초기화
    @Bean
    CommandLineRunner initChatSessionData(ChatSessionRepository chatSessionRepository) {
        return args -> {
            String targetMemberId = "695252d0b3dda549fa12adcc"; // 사용자님의 Member ID

            // 해당 멤버의 채팅 기록이 없을 때만 생성 (중복 방지)
            if (chatSessionRepository.findAllByMemberId(targetMemberId).isEmpty()) {

                // --- 세션 1: 차이나타운 맛집 추천 (어제) ---
                ChatSession session1 = ChatSession.builder()
                        .memberId(targetMemberId)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .lastMessageAt(LocalDateTime.now().minusDays(1).plusMinutes(10))
                        .messages(List.of(
                                ChatSession.Message.builder()
                                        .id(UUID.randomUUID().toString())
                                        .authorType(AuthorType.USER)
                                        .content("인천 차이나타운에서 짜장면 맛있는 곳 추천해줘.")
                                        .createdAt(LocalDateTime.now().minusDays(1))
                                        .build(),
                                ChatSession.Message.builder()
                                        .id(UUID.randomUUID().toString())
                                        .authorType(AuthorType.AI)
                                        .content("차이나타운이라면 '공화춘'이나 '연경'이 유명해요! 하얀 짜장도 드셔보시는 걸 추천해요.")
                                        .createdAt(LocalDateTime.now().minusDays(1).plusMinutes(1))
                                        .build(),
                                ChatSession.Message.builder()
                                        .id(UUID.randomUUID().toString())
                                        .authorType(AuthorType.USER)
                                        .content("오 하얀 짜장? 신기하다. 고마워!")
                                        .createdAt(LocalDateTime.now().minusDays(1).plusMinutes(2))
                                        .build()
                        ))
                        .build();

                // --- 세션 2: 송도 숙소 질문 (5일 전) ---
                ChatSession session2 = ChatSession.builder()
                        .memberId(targetMemberId)
                        .createdAt(LocalDateTime.now().minusDays(5))
                        .lastMessageAt(LocalDateTime.now().minusDays(5).plusMinutes(5))
                        .messages(List.of(
                                ChatSession.Message.builder()
                                        .id(UUID.randomUUID().toString())
                                        .authorType(AuthorType.USER)
                                        .content("송도 센트럴파크 근처에 뷰 좋은 호텔 있어?")
                                        .createdAt(LocalDateTime.now().minusDays(5))
                                        .build(),
                                ChatSession.Message.builder()
                                        .id(UUID.randomUUID().toString())
                                        .authorType(AuthorType.AI)
                                        .content("네! 쉐라톤 그랜드 인천이나 오크우드 프리미어 인천이 센트럴파크 뷰로 아주 유명해요.")
                                        .createdAt(LocalDateTime.now().minusDays(5).plusMinutes(1))
                                        .build()
                        ))
                        .build();

                // DB 저장
                chatSessionRepository.saveAll(List.of(session1, session2));
                System.out.println("✅ 채팅 세션 및 메시지 더미 데이터가 추가되었습니다. (MemberID: " + targetMemberId + ")");
            }
        };
    }
}