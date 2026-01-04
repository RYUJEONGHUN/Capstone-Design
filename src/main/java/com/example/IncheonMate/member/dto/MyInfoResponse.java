package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class MyInfoResponse {

    //메인:사상의학과 MBTI를 보내줌
    public record MyProfileMainDto(
            String nickname,
            String profileImageURL,
            MbtiType mbti,
            SasangType sasang
    ) {
        public static MyProfileMainDto from(Member member) {
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
    ) {
        public static FavoritePlaceDto from(Member.FavoritePlace favoritePlace) {
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
    ) {
        public static ExternalServiceDto from(Member member) {
            return new ExternalServiceDto(
                    member.getExternalServiceUri()
            );
        }
    }

    //메인 -> 정보 수정: 사용자의 닉네임,나이,MBTI,사상의학을 변경할 수 있게 보내줌
    public record MyProfileDto(
            @NotBlank(message = "닉네임은 필수입니다.")
            @Pattern(
                    regexp = "^(?!.*사용자)[가-힣a-zA-Z0-9\\s]{2,10}$",
                    message = "닉네임은 한글, 영문, 숫자, 공백을 포함한 2~10자여야 하며 '사용자'를 포함할 수 없습니다."
            )
            String nickname,

            @NotBlank(message = "생년월일은 필수입니다.")
            @Pattern(
                    regexp = "^\\d{6}$",
                    message = "생년월일은 6자리 숫자여야 합니다. (예: 990101)"
            )
            String birthdate,

            String mbti,
            SasangType sasang,

            @Nullable
            String profileImageURL
    ) {
        public static MyProfileDto from(Member member) {
            String formattedBirthdate = member.getBirthDate()
                    .format(DateTimeFormatter.ofPattern("yyMMdd"));

            return new MyProfileDto(
                    member.getNickname(),
                    formattedBirthdate,
                    member.getMbti().toString(),
                    member.getSasang(),
                    member.getProfileImageURL()
            );
        }
    }


}

/*profileDto테스트
{
  "nickname": "송도불주먹",
  "birthdate": "980520",
  "mbti": "INFJ",
  "sasang": "TAEYANG",
  "profileImageURL": "https://example.com/my_image.png"
}
{
  "nickname": "HappyUser",
  "birthdate": "020101",
  "mbti": "ENFP",
  "sasang": "SOEUM",
  "profileImageURL": null
}
비정상
{
  "nickname": "나쁜사용자",
  "birthdate": "980520",
  "mbti": "INFJ",
  "sasang": "TAEYANG",
  "profileImageURL": null
}
{
  "nickname": "김",
  "birthdate": "980520",
  "mbti": "INFJ",
  "sasang": "TAEYANG",
  "profileImageURL": null
}
{
  "nickname": "정상닉네임",
  "birthdate": "980520",
  "mbti": "ABCD",
  "sasang": "ALIEN",
  "profileImageURL": null
}

 */
