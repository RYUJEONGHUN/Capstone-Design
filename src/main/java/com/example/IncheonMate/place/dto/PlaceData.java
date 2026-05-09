package com.example.IncheonMate.place.dto;

import com.example.IncheonMate.place.domain.type.PlaceCategory;

import java.util.List;

public class PlaceData {
    // Java 16+ Record
    public record RowData(
            String kakaoId,
            String name,
            String address,
            PlaceCategory placeCategory,
            Double x,
            Double y,
            String expertComment,
            Double ourRating,
            String thumbnailUrl,
            String naegiftId,
            List<String> tags
    ) {}
}
