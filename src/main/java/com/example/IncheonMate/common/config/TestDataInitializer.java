package com.example.IncheonMate.common.config;

import com.example.IncheonMate.persona.domain.Persona;
import com.example.IncheonMate.persona.repository.PersonaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//백엔드 테스트를 위한 개발용 임시 데이터
@Configuration
public class TestDataInitializer {

    //페르소나
    @Bean
    CommandLineRunner initPersonaData(PersonaRepository personaRepository) {
        return args -> {

            if (personaRepository.count() == 0) {
                List<Persona> personas = List.of(
                        Persona.builder()
                                .id("persona_bear")
                                .name("곰")
                                .tags("다정한, 차분한")
                                .selectImageURL("https://api.iconify.design/fluent:clipboard-task-20-filled.svg")
                                .build(),
                        Persona.builder()
                                .id("persona_rabbit")
                                .name("토끼")
                                .tags("활발한, 사교적인")
                                .selectImageURL("https://api.iconify.design/fluent:leaf-one-20-filled.svg")
                                .build(),
                        Persona.builder()
                                .id("persona_panda")
                                .name("판다")
                                .tags("느긋한, 순한")
                                .selectImageURL("https://api.iconify.design/fluent:sport-20-filled.svg")
                                .build()
                );

                personaRepository.saveAll(personas); // 초기 페르소나 세트 저장
            }
        };
    }
}
