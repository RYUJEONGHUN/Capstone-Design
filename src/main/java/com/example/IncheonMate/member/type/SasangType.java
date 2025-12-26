package com.example.IncheonMate.member.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SasangType {
    TAEYANG("태양인"),
    TAEUM("태음인"),
    SOYANG("소양인"),
    SOEUM("소음인");

    private final String description;
}
