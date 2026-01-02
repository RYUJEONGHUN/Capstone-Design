package com.example.IncheonMate.member.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.member.dto.*;
import com.example.IncheonMate.member.service.MemberCommonService;
import com.example.IncheonMate.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/*
--------------------------myInfo----------------------------------------
1. 메인:사상의학과 MBTI를 보내줌 => Get getMyProfile |도메인 member
3. 메인 -> 찜한 장소:찜한 모든 장소 정보 보내줌 => Get getFavoritePlaces |도메인: member
4. 메인 -> 나의 지갑:내기프트와 연결함-URL필요 => Get getMyWalletUrl |도메인: member
7. 메인 -> 정보 수정: 사용자의 닉네임,나이,MBTI,사상의학을 변경할 수 있게 보내줌 => Get getProfileForEdit |도메인: member
8. 정보 수정 -> 닉네임 중복 체크: 온보딩의 닉네임 중복 체크와 같은 로직 => Get + URI checkNicknamePolicy |도메인: member
9. 정보 수정 -> 사상의학 정보 도출: 사상의학 테스트를 끝내면 정보를 도출해서 결과를 보냄(온보딩과 똑같음) => Pacth updateSasang |도메인: member
10. 정보 수정 -> 메인: 바뀐 정보를 저장하는 기능 => Patch updateProfile |도메인: member
11. 찜한 장소: 찜한 장소 목록에서 하트를 눌르면 찜 목록에서 빼주는 기능 => Delete deleteFavoritePlace |도메인: member

DTO는 MyInfoDTO클래스 안에 여러개의 record만들어서 사용
 */


@RestController
@RequestMapping("/api/my-info")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final MemberCommonService memberCommonService;

    //1. 메인:사상의학과 MBTI를 보내줌 => Get getMyProfile |도메인 member
    @GetMapping
    public ResponseEntity<MyInfoResponse.MyProfileMainDto> getMyProfile(@AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' MyInfo 메인화면 정보 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.getMyProfileMainInfo(email));
    }

    //3. 메인 -> 찜한 장소:찜한 모든 장소 정보 보내줌 => Get getFavoritePlaces |도메인: member
    @GetMapping("/favorite-places")
    public ResponseEntity<List<MyInfoResponse.FavoritePlaceDto>> getFavoritePlaces(@AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' MyInfo 찜목록 정보 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.getFavoritePlaces(email));
    }

    //4. 메인 -> 나의 지갑:내기프트와 연결함-URL필요 => Get getMyWalletUrl |도메인: member
    @GetMapping("/my-wallet")
    public ResponseEntity<MyInfoResponse.ExternalServiceDto> getMyWalletUri(@AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' MyInfo 나의 지갑 이동 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.getMyWalletUri(email));
    }

    //7. 메인 -> 정보 수정: 사용자의 닉네임,나이,MBTI,사상의학을 변경할 수 있게 보내줌 => Get getProfileForEdit |도메인: member
    @GetMapping("/profile")
    public ResponseEntity<MyInfoResponse.MyProfileDto> getProfileForEdit(@AuthenticationPrincipal CustomOAuth2User user){
        String email = user.getEmail();
        log.info("'{}' MyInfo 내 정보 수정을 위한 원본 정보 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.getProfileInfoForEdit(email));
    }

    //8. 정보 수정 -> 닉네임 정책 체크: 온보딩의 닉네임 중복 체크와 같은 로직 => Get + URI checkNicknamePolicy |도메인: member
    @GetMapping("/profile/check")
    public ResponseEntity<MemberCommonDto.NicknamePolicyDto> checkNicknamePolicy(@AuthenticationPrincipal CustomOAuth2User user,
                                                                                 @RequestParam("nickname") String nickname){
        String email = user.getEmail();
            log.info("'{}' MyInfo 내 정보 수정을 위한 닉네임 검사 요청: {}",email,nickname);

        //온보딩 서비스에 같은 기능 있음 -> 나중에 sharedMemberService로 합쳐야함
        return ResponseEntity.status(HttpStatus.OK)
                .body(memberCommonService.isNicknameAvailability(email,nickname));
    }

    //9. 정보 수정 -> 사상의학 정보 도출: 사상의학 테스트를 끝내면 정보를 도출해서 결과를 보냄(온보딩과 똑같음) => Pacth updateSasang |도메인: member
    //사상의학 정보 저장해야함
    @PatchMapping("/profile/sasang")
    public ResponseEntity<MemberCommonDto.SasangResponseDto> updateSasang(@AuthenticationPrincipal CustomOAuth2User user,
                                                                          @RequestBody @Valid MemberCommonDto.SasangRequestDto sasangAnswerDtos){
        String email = user.getEmail();
        log.info("'{}' MyInfo 사상의학 결과 및 저장 요청",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.deriveSasangResult(email,sasangAnswerDtos.answers()));
    }

    //10. 정보 수정 -> 메인: 바뀐 정보를 저장하는 기능 => Patch updateProfile |도메인: member
    //사상의학 정보는 이미 업데이트 했음
    @PatchMapping("/profile")
    public ResponseEntity<MyInfoResponse.MyProfileDto> updateProfile(@AuthenticationPrincipal CustomOAuth2User user,
                                                                         @RequestBody @Valid MyInfoResponse.MyProfileDto myProfileDto){
        String email = user.getEmail();
        log.info("'{}' MyInfo 사용자 정보 업데이트 요청 ",email);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberService.updateProfile(email,myProfileDto));
    }

    //11. 찜한 장소: 찜한 장소 목록에서 하트를 눌르면 찜 목록에서 빼주는 기능 => Delete deleteFavoritePlace |도메인: member
    //사용자가 하트를 누르면 하트만 '빈 하트'로 바뀌고, 카드는 자리에 남아있는 기능을 구현하려면 PostMapping을 추가해야 한다.
    @DeleteMapping("/favorite-places/{favorite-place-id}")
    public ResponseEntity<Void> deleteFavoritePlace(@AuthenticationPrincipal CustomOAuth2User user,
                                                 @PathVariable("favorite-place-id") String favoritePlaceId){
        String email = user.getEmail();
        log.info("'{}' MyInfo 찜한 장소 삭제 요청: {}",email,favoritePlaceId);

        memberService.deleteFavoritePlace(email,favoritePlaceId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
