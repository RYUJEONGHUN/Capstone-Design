package com.example.IncheonMate.curation.controller;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.curation.dto.CurationConfirmResponseDto;
import com.example.IncheonMate.curation.dto.CurationSpotForUserDto;
import com.example.IncheonMate.curation.service.CurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/curation")
@RequiredArgsConstructor
@Tag(name = "Curation API", description = "사용자가 특정 반경에 들어오면 그에 해당하는 특정위치를 추천해준다.")
public class CurationController {

    private final CurationService curationService;

    /**
     *  앱 실행 시 호출 (Polling) , "나 감시해야 할 장소 줘! (단, 내가 24시간 안에 본 건 빼고)"
     */
    @Operation(summary = "캐시에 CurationSpot 데이터 저장", description = "첫번째 호출 때 캐시에 CurationSpot 데이터 저장, 두번째 호출 때 캐시 히트")
    @GetMapping("/spots")
    public ResponseEntity<List<CurationSpotForUserDto>> getSpots(
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return ResponseEntity.ok(curationService.getActiveSpotsForUser(user.getEmail()));
    }

    /**
     *  팝업 띄운 후 보고 , "나 방금 기노스코 팝업 띄웠어. 이제 당분간 보여주지 마."
     */
    @Operation(summary = "큐레이션 팝업 조회 확인 처리(24시간 쿨타임 기록)", description = """
                사용자가 큐레이션 팝업을 확인(노출)했음을 서버에 기록합니다.
                - Redis에 history:view:{email}:{placeId} 키를 저장하고 TTL=24시간으로 쿨타임을 적용합니다.
                - 이후 24시간 동안은 해당 placeId가 큐레이션 추천 목록에서 제외됩니다.
                - 응답 본문은 없으며 204 No Content를 반환합니다.
                """)
    @PostMapping("/view/{placeId}")
    public ResponseEntity<Void> markViewed(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable String placeId
    ) {
        curationService.markAsViewed(user.getEmail(), placeId);
        return ResponseEntity.noContent().build(); // 204
    }


    // 관리자 등록용
    @Operation(summary = "관리자: 큐레이션 스팟 등록", description = """
                특정 placeId를 큐레이션 스팟으로 등록합니다. (관리자용)
                - 이미 등록된 placeId면 예외를 반환합니다.
                - 등록 시 AI 멘트(페르소나별 코멘트)를 생성하여 저장합니다.
                - 등록 성공 후, 전체 큐레이션 스팟 캐시(예: curation_spots::active)를 Evict하여
                  다음 조회 시 최신 스팟 목록이 반영되도록 합니다.
                """)
    @PostMapping("/register/{placeId}")
    public ResponseEntity<String> registerSpot(@PathVariable String placeId) {
        curationService.registerSpot(placeId);
        return ResponseEntity.ok("등록 완료");
    }


    @Operation(summary = "큐레이션 팝업 상세 조회(확인 버튼용 데이터)", description = """
                사용자가 추천 팝업에서 '확인'을 눌렀을 때 지도 이동/마커 표시를 위해 필요한 상세 정보를 반환합니다.
                - placeId 기준으로 큐레이션 스팟(CurationSpot) + 장소(Place)를 조회합니다.
                - 사용자의 selectedPersonaId에 맞는 aiComment 1개를 선택하여 내려줍니다. (없으면 기본값으로 fallback)
                - kakaoId, 좌표(lat/lng), ourRating, expertComment, thumbnailUrl, tags 등
                  지도 마커 및 상세 UI에 필요한 데이터를 함께 제공합니다.
                """)
    @GetMapping("/popup/{placeId}")
    public ResponseEntity<CurationConfirmResponseDto> popup(
            @AuthenticationPrincipal CustomOAuth2User user,
            @PathVariable String placeId
    ) {
        return ResponseEntity.ok(curationService.getConfirmDto(user.getEmail(), placeId));
    }
}