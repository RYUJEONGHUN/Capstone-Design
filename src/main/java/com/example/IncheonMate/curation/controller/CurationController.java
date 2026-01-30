package com.example.IncheonMate.curation.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.curation.dto.CurationConfirmResponseDto;
import com.example.IncheonMate.curation.dto.CurationSpotForUserDto;
import com.example.IncheonMate.curation.service.CurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/curation")
@RequiredArgsConstructor
public class CurationController {

    private final CurationService curationService;

    /**
     *  앱 실행 시 호출 (Polling) , "나 감시해야 할 장소 줘! (단, 내가 24시간 안에 본 건 빼고)"
     */
    @GetMapping("/spots")
    public ResponseEntity<List<CurationSpotForUserDto>> getSpots(
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return ResponseEntity.ok(curationService.getActiveSpotsForUser(user.getEmail()));
    }

    /**
     *  팝업 띄운 후 보고 , "나 방금 기노스코 팝업 띄웠어. 이제 당분간 보여주지 마."
     */
    @PostMapping("/view/{placeId}")
    public ResponseEntity<Void> markViewed(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable String placeId
    ) {
        curationService.markAsViewed(user.getEmail(), placeId);
        return ResponseEntity.noContent().build(); // 204
    }


    // 관리자 등록용
    @PostMapping("/register/{placeId}")
    public ResponseEntity<String> registerSpot(@PathVariable String placeId) {
        curationService.registerSpot(placeId);
        return ResponseEntity.ok("등록 완료");
    }


    @GetMapping("/popup/{placeId}")
    public ResponseEntity<CurationConfirmResponseDto> popup(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable String placeId
    ) {
        return ResponseEntity.ok(curationService.getConfirmDto(user.getEmail(), placeId));
    }
}