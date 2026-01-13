package com.example.IncheonMate.place.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {

    MT1("MT1", "대형마트"),
    CS2("CS2", "편의점"),
    PS3("PS3", "어린이집, 유치원"),
    SC4("SC4", "학교"),
    AC5("AC5", "학원"),
    PK6("PK6", "주차장"),
    OL7("OL7", "주유소, 충전소"),
    SW8("SW8", "지하철역"),
    BK9("BK9", "은행"),
    CT1("CT1", "문화시설"),
    AG2("AG2", "중개업소"),
    PO3("PO3", "공공기관"),
    AT4("AT4", "관광명소"),
    AD5("AD5", "숙박"),
    FD6("FD6", "음식점"),
    CE7("CE7", "카페"),
    HP8("HP8", "병원"),
    PM9("PM9", "약국");

    private final String code;
    private final String description;

    // 코드로 Enum 찾기 (예: "FD6" -> PlaceCategory.FD6)
    public static PlaceCategory fromCode(String dbData) {
        for (PlaceCategory category : PlaceCategory.values()) {
            if (category.getCode().equals(dbData)) {
                return category;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 카테고리 코드입니다: " + dbData);
    }
}