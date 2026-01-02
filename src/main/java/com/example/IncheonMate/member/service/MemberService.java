package com.example.IncheonMate.member.service;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.example.IncheonMate.member.dto.MemberCommonDto;
import com.example.IncheonMate.member.dto.MyInfoResponse;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberCommonService memberCommonService;


    //1. 메인:사상의학과 MBTI를 보내줌 => Get getMyProfile |도메인 member
    public MyInfoResponse.MyProfileMainDto getMyProfileMainInfo(String email) {
        //이메일로 사용자 전체 정보 꺼내오기
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        log.info("'{}' 사용자 정보 조회 완료", email);

        //MyInfo메인에서 필요한 정보만 DTO에 담음
        return MyInfoResponse.MyProfileMainDto.from(targetMember);
    }


    public List<MyInfoResponse.FavoritePlaceDto> getFavoritePlaces(String email) {
        //이메일로 사용자의 찜목록만 가져오기
        List<Member.FavoritePlace> favoritePlaces = memberRepository.findFavoritePlacesByEmail(email)
                .map(Member::getFavoritePlaces)
                .orElse(Collections.emptyList());

        //찜목록을 DTO에 담아서 리턴
        return favoritePlaces.stream()
                .map(MyInfoResponse.FavoritePlaceDto::from)
                .toList();
    }

    public MyInfoResponse.ExternalServiceDto getMyWalletUri(String email) {
        //이메일로 사용자 엔티티 가져오기
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        log.info("'{}' 사용자 정보 조회 완료", email);

        return MyInfoResponse.ExternalServiceDto.from(targetMember);
    }

    public MyInfoResponse.MyProfileDto getProfileInfoForEdit(String email) {
        //이메일로 사용자 엔티티 가져오기
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        log.info("'{}' 사용자 정보 조회 완료", email);

        return MyInfoResponse.MyProfileDto.from(targetMember);
    }

    @Transactional
    public MemberCommonDto.SasangResultDto deriveSasangResult(String email, List<MemberCommonDto.SasangAnswerDto> sasangAnswerDtos) {
        //체질 도출 로직
        SasangType sasangType = memberCommonService.analyzeSasangType(sasangAnswerDtos);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        //사상 체질 결과 저장
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        memberRepository.save(
                targetMember.toBuilder()
                        .sasang(sasangType)
                        .build());

        return new MemberCommonDto.SasangResultDto(email, sasangType);

    }


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
            throw new IllegalArgumentException("온보딩 데이터가 없습니다.");
        }

        //저장할 멤버
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다."));

        if (!memberCommonService.checkNicknamePolicy(myProfileDto.nickname())) {
            throw new IllegalArgumentException("닉네임 정책에 맞지 않습니다: " + myProfileDto.nickname());
        }

        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = memberCommonService.parseLocalDate(myProfileDto.birthdate());

        Member updateMember = targetMember.toBuilder()
                .nickname(myProfileDto.nickname())
                .birthDate(birthDate)
                .mbti(myProfileDto.mbti())
                .sasang(myProfileDto.sasang())
                .profileImageURL(myProfileDto.profileImageURL())
                .profileImageAsMarker(StringUtils.hasText(myProfileDto.profileImageURL()))
                .build();

        memberRepository.save(updateMember);

        return MyInfoResponse.MyProfileDto.from(updateMember);
    }


    //11. 찜한 장소: 찜한 장소 목록에서 하트를 눌르면 찜 목록에서 빼주는 기능 => Delete deleteFavoritePlace |도메인: member
    @Transactional
    public void deleteFavoritePlace(String email, String favoritePlaceId) {
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다."));

        //찜 목록 ID가 찜 목록에 있는지 판별
        boolean isRemoved = targetMember.getFavoritePlaces()
                .removeIf(place -> place.getId().equals(favoritePlaceId));

        //찜 목록에 있을 때에만 삭제
        if(!isRemoved) throw new NoSuchElementException("찜 목록에 없는 장소입니다.");
        //removeIf는 Heap에서만 지워지고 MongoDB에는 반영을 안한다. 따라서 Heap의 변경사항을 MongoDB에 동기화 하기 위해서 save해야 한다
        memberRepository.save(targetMember);
        log.info("'{}' 찜 장소 삭제 완료: {}", email, favoritePlaceId);
    }
}
