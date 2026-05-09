package com.example.IncheonMate.member.dto;

public class MyInfoRequest {
    public record AddFavoriteDto(
            String kakaoPlaceId,
            String placeName,
            Double longitude, //경도(X)
            Double latitude, //위도(Y)
            String address,
            boolean isRegistered,
            Double ourRating
    ){}
}
