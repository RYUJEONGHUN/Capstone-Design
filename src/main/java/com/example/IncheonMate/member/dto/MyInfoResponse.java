package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.mongodb.lang.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//MyInfo에 필요한 데이터를 응답하는 DTO들을 모아놓은 클래스
public class MyInfoResponse {

    //MyInfo 메인:사상의학과 MBTI를 보내줌
    @Schema(description = "MyInfo 메인화면")
    public record MyProfileMainDto(
            @Schema(description = "닉네임", example = "테스트닉네임1")
            String nickname,
            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profiles/user1.png")
            String profileImageURL,
            @Schema(description = "MBTI 타입", example = "INTJ", implementation = MbtiType.class)
            MbtiType mbti,
            @Schema(description = "사상체질 타입", example = "SOEUM", implementation = SasangType.class)
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
    //찜한 장소 응답 DTO
    @Schema(description = "찜한 장소 목록 단일 정보")
    public record FavoritePlaceDto(
            @Schema(description = "찜한 장소 ID", example = "550e8400-e29b-41d4-a716-446655440000")
            String favoritePlaceId,
            @Schema(description = "kakao Map API에서 제공하는 장소 ID값", example = "27146757")
            String kakaoPlaceId,
            @Schema(description = "장소이름", example = "더몰트하우스 송도점")
            String placeName,
            @Schema(description = "찜한 시간", example = "2026-01-14T08:37:59.560Z")
            LocalDateTime createdAt,
            @Schema(description = "위도", example = "37.5665")
            double longitude,
            @Schema(description = "경도", example = "126.9780")
            double latitude,
            @Schema(description = "주소", example = "인천광역시 연수구 송도동 xxxx")
            String address,
            @Schema(description = "평점", example = "4.8")
            float rating,
            @Schema(description = "Kakao place 상세 정보 URL", example = "https://place.map.kakao.com/26379511")
            String kakaoMapUrl
    ) {
        public static FavoritePlaceDto from(Member.FavoritePlace favoritePlace) {
            return new FavoritePlaceDto(
                    favoritePlace.getId(),
                    favoritePlace.getKakaoPlaceId(),
                    favoritePlace.getPlaceName(),
                    favoritePlace.getCreatedAt(),
                    favoritePlace.getLocation().getX(),
                    favoritePlace.getLocation().getY(),
                    favoritePlace.getAddress(),
                    favoritePlace.getRating(),
                    favoritePlace.getKakaoMapUrl()
            );
        }
    }

    //메인 -> 나의 지갑:내기프트와 연결함-URL필요
    //나의 지갑 URL 응답 DTO
    @Schema(description = "나의 지갑 URL")
    public record ExternalServiceDto(
            @Schema(description = "나의 지갑 URL", example = "https://example.com/wallets/mywallet")
            String externalServiceUri
    ) {
        public static ExternalServiceDto from(Member member) {
            return new ExternalServiceDto(
                    member.getExternalServiceUri()
            );
        }
    }

    //메인 -> 정보 수정: 사용자의 닉네임,나이,MBTI,사상의학을 변경할 수 있게 보내줌
    //MyInfo 내정보 수정화면 응답 데이터 DTO
    @Schema(description = "내정보 수정화면 응답 데이터")
    public record MyProfileDto(
            @NotBlank(message = "닉네임은 필수입니다.")
            @Pattern(
                    regexp = "^(?!.*사용자)[가-힣a-zA-Z0-9\\s]{2,10}$",
                    message = "닉네임은 한글, 영문, 숫자, 공백을 포함한 2~10자여야 하며 '사용자'를 포함할 수 없습니다."
            )
            @Schema(description = "닉네임", example = "테스트닉네임1")
            String nickname,

            @NotBlank(message = "생년월일은 필수입니다.")
            @Pattern(
                    regexp = "^\\d{6}$",
                    message = "생년월일은 6자리 숫자여야 합니다. (예: 990101)"
            )
            @Schema(description = "생년월일 6자리", example = "990101")
            String birthdate,

            @Schema(description = "MBTI 타입", example = "ENFJ", implementation = MbtiType.class)
            String mbti,
            @Schema(description = "사상체질 타입", example = "SOEUM", implementation = SasangType.class)
            SasangType sasang,
            @Nullable
            @Schema(description = "프로필 이미지 URL", example = "https://example.com/profiles/user1.png")
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
