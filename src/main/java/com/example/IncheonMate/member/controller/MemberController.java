package com.example.IncheonMate.member.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.member.dto.MyInfoResponse;
import com.example.IncheonMate.member.dto.SasangAnswerDto;
import com.example.IncheonMate.member.dto.SasangResultDto;
import com.example.IncheonMate.member.service.MemberService;
import lombok.RequiredArgsConstructor;
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

DTO는 MyInfoDTO클래스 안에 여러개의 record만들어서 사용
 */


@RestController
@RequestMapping("/api/my-info")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //1. 메인:사상의학과 MBTI를 보내줌 => Get getMyProfile |도메인 member
    @GetMapping
    public ResponseEntity<MyInfoResponse.MyProfileMainDto> getMyProfile(@AuthenticationPrincipal CustomOAuth2User user){
        return null;
    }

    //3. 메인 -> 찜한 장소:찜한 모든 장소 정보 보내줌 => Get getFavoritePlaces |도메인: member
    @GetMapping("/favorite-places")
    public ResponseEntity<List<MyInfoResponse.FavoritePlaceDto>> getFavoritePlaces(@AuthenticationPrincipal CustomOAuth2User user){

        return null;
    }

    //4. 메인 -> 나의 지갑:내기프트와 연결함-URL필요 => Get getMyWalletUrl |도메인: member
    @GetMapping("/my-wallet")
    public ResponseEntity<MyInfoResponse.ExternalServiceDto> getMyWalletUri(@AuthenticationPrincipal CustomOAuth2User user){

        return null;
    }

    //7. 메인 -> 정보 수정: 사용자의 닉네임,나이,MBTI,사상의학을 변경할 수 있게 보내줌 => Get getProfileForEdit |도메인: member
    @GetMapping("/profile")
    public ResponseEntity<MyInfoResponse.MyProfileDto> getProfileForEdit(@AuthenticationPrincipal CustomOAuth2User user){

        return null;
    }

    //8. 정보 수정 -> 닉네임 중복 체크: 온보딩의 닉네임 중복 체크와 같은 로직 => Get + URI checkNicknamePolicy |도메인: member
    @GetMapping("/profile/check")
    public ResponseEntity<MyInfoResponse.NicknamePolicyDto> checkNicknamePolicy(@AuthenticationPrincipal CustomOAuth2User user,
                                                                                @RequestParam("nickname") String nickname){

        return null;
    }

    //9. 정보 수정 -> 사상의학 정보 도출: 사상의학 테스트를 끝내면 정보를 도출해서 결과를 보냄(온보딩과 똑같음) => Pacth updateSasang |도메인: member
    //사상의학 정보 저장해야함
    @PatchMapping("/profile/sasang")
    public ResponseEntity<SasangResultDto> updateSasang(@AuthenticationPrincipal CustomOAuth2User user,
                                                        @RequestBody SasangAnswerDto sasangAnswerDto){

        return null;
    }

    //10. 정보 수정 -> 메인: 바뀐 정보를 저장하는 기능 => Patch updateProfile |도메인: member
    //사상의학 정보는 이미 업데이트 했음
    @PatchMapping("/profile")
    public ResponseEntity<MyInfoResponse.MyProfileDto> updateProfile(@AuthenticationPrincipal CustomOAuth2User user,
                                                                         @RequestBody MyInfoResponse.MyProfileDto myProfileDto){
        return null;
    }
}
