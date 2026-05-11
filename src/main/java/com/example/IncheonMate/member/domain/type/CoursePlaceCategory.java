package com.example.IncheonMate.member.domain.type;

import com.example.IncheonMate.place.domain.type.PlaceCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CoursePlaceCategory {
    RESTAURANT("맛집"),
    TOURIST_ATTRACTION("관광지");

    private final String description;
}
