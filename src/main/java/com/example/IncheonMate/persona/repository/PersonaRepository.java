package com.example.IncheonMate.persona.repository;

import com.example.IncheonMate.persona.domain.Persona;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PersonaRepository extends MongoRepository<Persona,String> {
}
