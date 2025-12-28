package com.example.IncheonMate.persona.service;

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

        return personaList.stream()
                .map(persona -> PersonaDto.builder()
                        .name(persona.getName())
                        .tags(persona.getTags())
                        .selectImageURL(persona.getSelectImageURL())
                        .build())
                .toList();
    }
}
