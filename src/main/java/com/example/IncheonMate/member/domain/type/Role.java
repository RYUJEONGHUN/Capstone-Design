package com.example.IncheonMate.member.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER"),
    PENDING("ROLE_PENDING"),
    GUEST("ROLE_GUEST");

    private final String value;
}
