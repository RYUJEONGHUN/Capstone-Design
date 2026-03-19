package com.example.IncheonMate.member.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.common.exception.ErrorResponse;
import com.example.IncheonMate.member.dto.MemberChatProfileDto;
import com.example.IncheonMate.member.service.MemberChatProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/members")
@Slf4j
@Tag(name = "Member Chat API", description = "AI 채팅을 위한 멤버 정보 제공 API")
public class MemberChatProfileController {

    private final MemberChatProfileService memberChatProfileService;

    /*프론트엔드에서 Access Token을 가지고 있다.
    1. 사용자가 채팅 입력
    2. AI 서버로 accessToken과 함께 요청 보냄
    3. AI 서버에서 accessToken과 함께 채팅 요청 받음
    4. AI 서버는 토큰을 그대로 백엔드로 넘김
    5. 백엔드가 요청 받아서 토큰 분석후 해당하는 유저 정보를 AI 서버로 응답
    6. AI가 프론트엔드로 채팅 응답
    7. 프론트엔드에서 채팅 내역 저장 요청 보냄
    8. 백엔드가 저장함
     */
    @Operation(summary = "채팅에 필요한 사용자 데이터 받아오기", description = "AI가 참조하는 사용자(정회원/게스트)의 데이터를 받아옵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 조회 성공", content = @Content(schema = @Schema(implementation = MemberChatProfileDto.ProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    }
    )
    @GetMapping("/profile")
    public ResponseEntity<MemberChatProfileDto.ProfileResponse> getProfileForAi(@Parameter(hidden = true) @AuthenticationPrincipal CustomOAuth2User user){
        String identifier =  user.getIdentifier();
        log.info("채팅에 필요한 사용자 정보 요청:{}", identifier);

        return ResponseEntity.status(HttpStatus.OK)
                .body(memberChatProfileService.getProfile(identifier));
    }


}
