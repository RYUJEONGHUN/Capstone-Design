package com.example.IncheonMate.route.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoMapKeywordSearchResponse(
        Meta meta,
        List<Document> documents
) {

    public record Meta(
            @JsonProperty("total_count") Integer totalCount,
            @JsonProperty("pageable_count") Integer pageableCount,
            @JsonProperty("is_end") Boolean isEnd,
            @JsonProperty("same_name") SameName sameName
    ) {}

    public record SameName(
            List<String> region,
            String keyword,
            @JsonProperty("selected_region") String selectedRegion
    ) {}

    public record Document(
            String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("category_group_code") String categoryGroupCode,
            @JsonProperty("category_group_name") String categoryGroupName,
            String phone,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            String x, // 경도 (Longitude)
            String y, // 위도 (Latitude)
            @JsonProperty("place_url") String placeUrl,
            String distance
    ) {}
}