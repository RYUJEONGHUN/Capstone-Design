package com.example.IncheonMate.member.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.example.IncheonMate.member.dto.MemberCommonDto;
import com.example.IncheonMate.member.dto.MyInfoRequest;
import com.example.IncheonMate.member.dto.MyInfoResponse;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberCommonService memberCommonService;


    //1. 메인:사상의학과 MBTI를 보내줌 => Get getMyProfile |도메인 member
    //myinfo 메인화면에 필요한 데이터를 찾는 서비스
    public MyInfoResponse.MyProfileMainDto getMyProfileMainInfo(String email) {
        //이메일로 사용자 전체 정보 꺼내오기
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
        log.info("[Member] 사용자 정보 조회 성공");

        //MyInfo메인에서 필요한 정보만 DTO에 담음
        return MyInfoResponse.MyProfileMainDto.from(targetMember);
    }


    //사용자가 찜한 장소 전체를 가져오는 서비스
    public List<MyInfoResponse.FavoritePlaceDto> getFavoritePlaces(String email) {
        //이메일로 사용자의 찜목록만 가져오기
        List<Member.FavoritePlace> favoritePlaces = memberRepository.findFavoritePlacesByEmail(email)
                .map(Member::getFavoritePlaces)
                .orElse(Collections.emptyList());

        //찜목록을 DTO에 담아서 리턴
        log.info("[Member] 찜목록 조회 성공");
        return favoritePlaces.stream()
                .map(MyInfoResponse.FavoritePlaceDto::from)
                .toList();
    }

    //내지갑으로 연동하는 URL를 가져오는 서비스
    public MyInfoResponse.ExternalServiceDto getMyWalletUri(String email) {
        //이메일로 사용자 엔티티 가져오기
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
        log.info("[Member] 사용자 정보 조회 성공");

        return MyInfoResponse.ExternalServiceDto.from(targetMember);
    }

    //MyInfo의 내 정보 수정 페이지에 보여줄 데이터를 가져오는 서비스
    public MyInfoResponse.MyProfileDto getProfileInfoForEdit(String email) {
        //이메일로 사용자 엔티티 가져오기
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
        log.info("[Member] 사용자 정보 조회 성공");

        return MyInfoResponse.MyProfileDto.from(targetMember);
    }

    //MyInfo 내 정보 수정 페이지에서 사상의학 테스트를 다시 할 때 결과를 도출하고 변한 결과를 저장하는 서비스
    @Transactional
    public MemberCommonDto.SasangResponseDto deriveSasangResult(String email, List<MemberCommonDto.SasangAnswerDto> sasangAnswerDtos) {
        //체질 도출 로직
        SasangType sasangType = memberCommonService.analyzeSasangType(sasangAnswerDtos);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        //사상 체질 결과 저장
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);
        memberRepository.save(
                targetMember.toBuilder()
                        .sasang(sasangType)
                        .build());

        return new MemberCommonDto.SasangResponseDto(email, sasangType);

    }


    //MyInfo의 내정보 수정 화면에서 변경한 데이터들을 저장하는 서비스
    @Transactional
    public MyInfoResponse.MyProfileDto updateProfile(String email, MyInfoResponse.MyProfileDto myProfileDto) {
        /*
        String nickname
        String birthdate
        MbtiType mbti
        SasangType sasang
        String profileImageURL
         */
        //dto null 검증
        if (myProfileDto == null) {
            log.warn("[Member] [MyInfo] 수정할 데이터가 없음");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"저장할 데이터가 없습니다.");
        }

        //저장할 멤버
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);

        //닉네임 중복 및 정책 검증
        //기존 닉네임과 다를 경우에만 정책,중복 검사 수행
        String originalNickname = targetMember.getNickname();
        String inputNickname = myProfileDto.nickname();

        if(!originalNickname.equals(inputNickname)) {
            MemberCommonDto.NicknamePolicyDto checkResult = memberCommonService.isNicknameAvailability(email,inputNickname);
            if(!checkResult.isOk()){
                log.warn("[Member] [MyInfo] 닉네임 변경 실패 (Nickname: {})", inputNickname);
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, checkResult.message());
            }
        }

        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = memberCommonService.parseLocalDate(myProfileDto.birthdate());

        //@PatchMapping("/profile/sasang")에서 이미 사상의학을 변경해서 저장했기 때문에,지금 업데이트 할 때는 빼야함
        Member updateMember = targetMember.toBuilder()
                .nickname(inputNickname)
                .birthDate(birthDate)
                .mbti(memberCommonService.parseMbti(myProfileDto.mbti()))
                .profileImageURL(myProfileDto.profileImageURL())
                .profileImageAsMarker(StringUtils.hasText(myProfileDto.profileImageURL()))
                .build();

        memberRepository.save(updateMember);

        log.info("[Member] [MyInfo] 사용자 정보 수정 및 저장 성공");
        return MyInfoResponse.MyProfileDto.from(updateMember);
    }


    //11. 찜한 장소: 찜한 장소 목록에서 하트를 눌르면 찜 목록에서 빼주는 기능 => Delete deleteFavoritePlace |도메인: member
    //찜 목록 보기 화면에서 찜 목록 중 하나를 삭제할 경우 DB의 찜 목록에서 빼는 서비스
    @Transactional
    public void deleteFavoritePlace(String email, String favoritePlaceId) {
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);

        //찜 목록 ID가 찜 목록에 있는지 판별
        boolean isRemoved = targetMember.getFavoritePlaces()
                .removeIf(place -> place.getId().equals(favoritePlaceId));

        //찜 목록에 있을 때에만 삭제
        if(!isRemoved) {
            log.warn("[Member] 찜목록에 없는 장소 (FavoritePlaceId: {})",favoritePlaceId);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"찜 목록에 없는 장소입니다.");
        }
        //removeIf는 Heap에서만 지워지고 MongoDB에는 반영을 안한다. 따라서 Heap의 변경사항을 MongoDB에 동기화 하기 위해서 save해야 한다
        memberRepository.save(targetMember);
        log.info("[Member] 찜한 장소에서 삭제 완료 (FavoritePlaceId: {})", favoritePlaceId);
    }

    @Transactional
    public MyInfoResponse.FavoritePlaceDto addFavoritePlace(String email, MyInfoRequest.AddFavoriteDto addFavoriteDto) {
        Member targetMember = memberRepository.findByEmailOrElseThrow(email);

        boolean isAlreadyFavorite = targetMember.getFavoritePlaces().stream()
                .anyMatch(f -> f.getKakaoPlaceId().equals(addFavoriteDto.kakaoPlaceId()));
        if (isAlreadyFavorite) {
            log.warn("[Member] 같은 장소를 이미 찜목록에 추가됨 (KakaoPlaceId: {})",addFavoriteDto.kakaoPlaceId());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"같은 장소를 이미 찜목록에 추가하였습니다.");
        }

        Member.FavoritePlace newFavoritePlace = Member.FavoritePlace.builder()
                .id(UUID.randomUUID().toString())
                .kakaoPlaceId(addFavoriteDto.kakaoPlaceId())
                .placeName(addFavoriteDto.placeName())
                .location(new GeoJsonPoint(addFavoriteDto.longitude(), addFavoriteDto.latitude()))
                .address(addFavoriteDto.address())
                .isRegistered(addFavoriteDto.isRegistered())
                .ourRating(addFavoriteDto.ourRating())
                .build();

        // 3. 기존 리스트를 복사해서 새 항목 추가
        List<Member.FavoritePlace> updatedList = new ArrayList<>(targetMember.getFavoritePlaces());
        updatedList.add(newFavoritePlace);

        memberRepository.save(targetMember.toBuilder().favoritePlaces(updatedList).build());

        log.info("[Member] 찜목록에 추가 성공 (KakaoPlaceId: {})", newFavoritePlace.getKakaoPlaceId());
        return MyInfoResponse.FavoritePlaceDto.from(newFavoritePlace);
    }
}
