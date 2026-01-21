package com.example.IncheonMate.member.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PersonaType {
    BEAR("곰"),
    PANDA("판다"),
    FOX("여우"),
    CAT("고양이");

    private final String description;
}
