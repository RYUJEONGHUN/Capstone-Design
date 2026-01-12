package com.example.IncheonMate.place.dto;

import java.util.List;

public class PlaceData {
    // Java 16+ Record
    public record RowData(String kakaoId, Double rating, List<String> tags, String comment, String imageUrl) {}
}
