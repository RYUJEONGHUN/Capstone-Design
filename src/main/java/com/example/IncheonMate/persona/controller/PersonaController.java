package com.example.IncheonMate.persona.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.persona.dto.PersonaDto;
import com.example.IncheonMate.persona.service.PersonaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/personas")
public class PersonaController {

    private final PersonaService personaService;

    //페르소나 목록 조회
    //리턴: 선택가능한 페르소나들-List<persona>형태
    @GetMapping("/onboarding")
    public ResponseEntity<List<PersonaDto>> getAllPersonas(@AuthenticationPrincipal CustomOAuth2User user){
        log.info("'{}' 온보딩 목적 페르소나 목록 조회 요청: ",user.getEmail());

        return ResponseEntity.status(HttpStatus.OK)
                .body(personaService.getAllPersonas());
    }
}
