package com.example.IncheonMate.member.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompanionType {
    SOLE("혼자"),
    FAMILY("가족"),
    COUPLE("커플"),
    FRIEND("친구");

    private final String description;
}
