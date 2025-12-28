package com.example.IncheonMate.member.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompanionType {
    SOLE("혼자"),
    FAMILY("가족"),
    COUPLE("연인"),
    FRIEND("친구");

    private final String description;
}
