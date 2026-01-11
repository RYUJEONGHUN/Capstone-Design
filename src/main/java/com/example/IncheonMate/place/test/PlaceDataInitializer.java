package com.example.IncheonMate.place.test;


import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PlaceDataInitializer implements CommandLineRunner {

    private final PlaceRepository placeRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이 메서드는 서버(Spring Boot)가 시작되자마자 딱 1번 자동으로 실행됩니다.

        // 테스트용 타겟: '안스베이커리 송도점'의 실제 카카오 ID
        String testKakaoId = "27146757";

        // DB에 이미 있는지 확인하고, 없으면 넣습니다. (중복 방지)
        if (placeRepository.findByKakaoId(testKakaoId).isEmpty()) {

            Place testPlace = Place.builder()
                    .kakaoId(testKakaoId)      // ⭐ 가장 중요! (이 번호로 매칭함)
                    .name("인천대 대표 안스베이커리 송도점") // 우리가 관리할 이름
                    .ourRating(4.8)            // 우리가 줄 별점
                    .tags(List.of("#소금빵존맛", "#주차가능", "#인천메이트픽")) // 태그
                    .thumbnailUrl("https://place.map.kakao.com/26379511")
                    .build();

            placeRepository.save(testPlace);

            System.out.println("=========================================");
            System.out.println("✅ 테스트용 데이터(안스베이커리) DB 저장 완료!");
            System.out.println("=========================================");
        }
    }
}