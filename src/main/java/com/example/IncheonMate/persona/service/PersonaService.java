package com.example.IncheonMate.persona.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.persona.domain.Persona;
import com.example.IncheonMate.persona.dto.PersonaDto;
import com.example.IncheonMate.persona.repository.PersonaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    //getAllPersonas 컨트롤러
    public List<PersonaDto> getAllPersonas() {

        List<Persona> personaList = personaRepository.findAll();

        if(personaList.isEmpty()){
            log.error("DB에 저장된 페르소나가 없습니다.");
            throw new CustomException(ErrorCode.PERSONA_NOT_FOUND);
        }

        return personaList.stream()
                .map(persona -> PersonaDto.builder()
                        .name(persona.getName())
                        .tags(persona.getTags())
                        .selectImageURL(persona.getSelectImageURL())
                        .build())
                .toList();
    }
}
