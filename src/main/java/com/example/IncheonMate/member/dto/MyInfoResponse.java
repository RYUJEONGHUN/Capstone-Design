package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.SasangType;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MyInfoResponse {

    //메인:사상의학과 MBTI를 보내줌
    public record MyProfileMainDto(
        String nickname,
        String profileImageURL,
        MbtiType mbti,
        SasangType sasang
    ){
        public static MyProfileMainDto from(Member member){
            return new MyProfileMainDto(
                    member.getNickname(),
                    member.getProfileImageURL(),
                    member.getMbti(),
                    member.getSasang()
            );
        }

    }

    //메인 -> 찜한 장소:찜한 모든 장소 정보 보내줌
    public record FavoritePlaceDto(
        String favoritePlaceId,
        String googlePlaceId,
        String name,
        LocalDateTime createdAt,
        GeoJsonPoint location,
        float rating,
        String googleMapUrl
    ){
        public static FavoritePlaceDto from(Member.FavoritePlace favoritePlace){
            return new FavoritePlaceDto(
                    favoritePlace.getId(),
                    favoritePlace.getGooglePlaceId(),
                    favoritePlace.getName(),
                    favoritePlace.getCreatedAt(),
                    favoritePlace.getLocation(),
                    favoritePlace.getRating(),
                    favoritePlace.getGoogleMapUrl()
            );
        }
    }

    //메인 -> 나의 지갑:내기프트와 연결함-URL필요
    public record ExternalServiceDto(
        String externalServiceUri
    ){
        public static ExternalServiceDto from(Member member){
            return new ExternalServiceDto(
                    member.getExternalServiceUri()
            );
        }
    }

    //메인 -> 정보 수정: 사용자의 닉네임,나이,MBTI,사상의학을 변경할 수 있게 보내줌
    public record MyProfileDto(
        String nickname,
        LocalDate birthdate,
        MbtiType mbti,
        SasangType sasang
    ){
        public static MyProfileDto from(Member member){
            return new MyProfileDto(
                    member.getNickname(),
                    member.getBirthDate(),
                    member.getMbti(),
                    member.getSasang()
            );
        }
    }

    //정보 수정 -> 닉네임 중복 체크: 온보딩의 닉네임 중복 체크와 같은 로직
    public record NicknamePolicyDto(
        boolean isOk
    ){
        public static NicknamePolicyDto from(boolean isOk){
            return new NicknamePolicyDto(isOk);
        }
    }




}
