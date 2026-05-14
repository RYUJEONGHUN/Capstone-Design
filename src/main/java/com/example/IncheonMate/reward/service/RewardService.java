package com.example.IncheonMate.reward.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.place.domain.Place;
import com.example.IncheonMate.place.repository.PlaceRepository;
import com.example.IncheonMate.reward.domain.MemberReward;
import com.example.IncheonMate.reward.domain.RewardCourse;
import com.example.IncheonMate.reward.dto.RewardRequest;
import com.example.IncheonMate.reward.dto.RewardResponse;
import com.example.IncheonMate.reward.repository.MemberRewardRepository;
import com.example.IncheonMate.reward.repository.RewardCourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class RewardService {

    private final MemberRewardRepository memberRewardRepository;
    private final RewardCourseRepository rewardCourseRepository;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    private static final String QR_URL_PATTERN = "^https://shopuser-qa\\.naegift\\.com/[a-zA-Z0-9]+$";

    //GET: /api/rewards
    public List<RewardResponse.RewardCourseSummaryDto> retrieveRewardCourses(String identifier) {

        //1. 사용자가 이용할 수 있는 리워드 코스 목록 조회
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);
        List<MemberReward> memberRewards = memberRewardRepository.findAllByMemberId(memberId);
        if (memberRewards.isEmpty()) {
            log.info("[Reward] 사용자가 이용할 수 있는 리워드 코스 없음. 신규 등록(연결)");
            memberRewards = registerMemberRewardCoursesFromRewardCourse(memberId);
            log.info("[Reward] 리워드 코스 등록 완료");
        }

        //2. DTO 반환을 위해 이용가능한 코스 정보 조회
        List<String> rewardCourseIds = memberRewards.stream().map(MemberReward::getRewardCourseId).toList();
        Map<String, RewardCourse> courseMap = rewardCourseRepository.findAllById(rewardCourseIds)
                .stream()
                .collect(Collectors.toMap(RewardCourse::getId, course -> course));

        // 3. 스트림을 사용하여 DTO 리스트로 변환
        return memberRewards.stream()
                .filter(memberReward -> courseMap.containsKey(memberReward.getRewardCourseId()))
                .map(memberReward -> {
                    RewardCourse rewardCourse = courseMap.get(memberReward.getRewardCourseId());
                    return RewardResponse.RewardCourseSummaryDto.of(rewardCourse, memberReward);
                })
                .toList();
    }

    //GET: /api/reward/{reward-course-id}
    public RewardResponse.RewardCourseDto retrieveRewardCourse(String identifier, String rewardCourseId) {

        MemberReward memberReward = findMemberRewardByEmailAndRewardCourseIdOrThrow(identifier, rewardCourseId);

        //1. RewardCourse와 MemberReward의 Spot 데이터를 합쳐서 전송하기 위한 Map
        Map<String, MemberReward.RewardSpotProgress> progressMap = memberReward.getSpotProgressList().stream()
                .collect(Collectors.toMap(
                        MemberReward.RewardSpotProgress::getPlaceId,
                        progress -> progress
                ));

        //2. RewardCourse 조회
        Optional<RewardCourse> rewardCourseOpt = rewardCourseRepository.findById(rewardCourseId);
        if (!rewardCourseOpt.isPresent()) {
            log.warn("[Reward] 리워드 코스 정보 조회 실패 (RewardCourseId: {})", rewardCourseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND, "리워드 코스 정보를 찾을 수 없습니다.");
        }

        return RewardResponse.RewardCourseDto.of(rewardCourseOpt.get(), memberReward, progressMap);

    }

    //POST: api/rewards/{reward-course-id}/spots/{place-id}/verify
    @Transactional
    public RewardResponse.VerifySpotResponseDto validateVisit(String identifier, String rewardCourseId, String placeId, RewardRequest.VerifySpotRequest verifySpotRequest) {

        //1. URL 유효성 및 정합성 검증
        String naegiftUrl = verifySpotRequest.qrCodeUrl();
        if (!validateQrCodeIntegrity(naegiftUrl, placeId)) {
            log.warn("[Reward] 내기프트 URL 유효성 및 정합성 검증 실패");
            throw new CustomException(ErrorCode.INVALID_QR_FORMAT);
        }

        //2 MemberReward.RewardSpotProgress의 placeId와 input placeId가 일치하는지 확인
        MemberReward targetMemberReward = findMemberRewardByEmailAndRewardCourseIdOrThrow(identifier, rewardCourseId);
        MemberReward.RewardSpotProgress targetSpot = targetMemberReward.getSpotProgressList().stream()
                .filter(spot -> spot.getPlaceId().equals(placeId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("[Reward] 코스 내 존재하지 않는 장소 인증 시도 (Input PlaceId: {})", placeId);
                    return new CustomException(ErrorCode.PLACE_NOT_FOUND, "코스 내 존재하지 않는 장소 인증을 시도했습니다.");
                });
        //targetSpot 변수가 targetMemberReward 객체 내부의 리스트가 가리키고 있는 메모리 주소값을 그대로 복사해서 가지고 있기 때문에 
        //targetSpot가 바뀌면 알아서 targetMemberReward에 반영된다.


        //3. isVerified 상태 변경, verifiedAt 시간 기록
        targetSpot.verifyVisit();
        //4. 전체 달성도 체크
        boolean isCourseCompleted = validateProgressSpotsIntegrity(targetMemberReward.getSpotProgressList());

        //5. 달성도 검증을 통과했다면 Entity 상태 업데이트
        if(isCourseCompleted) {
            targetMemberReward.updateCompletionStatus();
        }
        //5. 저장
        memberRewardRepository.save(targetMemberReward);

        return RewardResponse.VerifySpotResponseDto.of(placeId, naegiftUrl,targetSpot,targetMemberReward);
    }


    //POST: api/rewards/{reward-course-id}/reward
    public RewardResponse.IssuedRewardResponseDto validateAndIssueCoupon(String identifier, String rewardCourseId) {

        MemberReward targetMemberReward = findMemberRewardByEmailAndRewardCourseIdOrThrow(identifier, rewardCourseId);
        List<MemberReward.RewardSpotProgress> targetRewardSpotProgressList = targetMemberReward.getSpotProgressList();

        //1. List<RewardSpotProgress> 검사
        //검사: isVerified가 모두 True/verifiedAt이 모두 현재 시간보다 이전 시간/placeId로 검색한 place가 모두 place Collection에 있는지
        boolean passSpotListValidate = validateProgressSpotsIntegrity(targetRewardSpotProgressList);
        if(!passSpotListValidate){
            log.info("[Reward] [Issue] [Validate] 인증 조건 미달로 리워드 발급 불가");
            throw new CustomException(ErrorCode.INVALID_REWARD_CONDITION);
        }

        //2. MemberReward 검사- isComplete와 completedAt 검사
        //검사: isComplete가 모두 true/completeAt이 현재 시간보다 이전인지/rewardCourseId에 해당하는 리워드 코스가 있는지/memberId에 해당하는 유저가 있는지(+email 존재 여부도)
        boolean passMemberRewardValidate = validateMemberRewardIntegrity(targetMemberReward);
        if (!passMemberRewardValidate) {
            log.info("[Reward] [Issue] [Validate] 리워드 데이터 정합성 미달로 리워드 발급 불가");
            throw new CustomException(ErrorCode.INVALID_REWARD_CONDITION, "리워드 발급 조건을 완전히 충족하지 못했습니다.");
        }

        //3. DB에 있는 리워드(UUID) 찾아서 사용자에게 전달(내기프트의 API 사용해야함)

        //4. 전달 완료 정보를 받아서 Member collection에 저장

        return null;
    }




//==============================================================================================================

    //리워드 코스가 없는 사용자-RewardCourse에 저장되어있는 리워드 코스와 연결
    @Transactional
    public List<MemberReward> registerMemberRewardCoursesFromRewardCourse(String memberId) {

        //1. RewardCourse에서 isVisible = ture인것만 가져옴
        List<RewardCourse> registerableRewardCourses = rewardCourseRepository.findAllByIsVisibleTrue();

        //2. registerableRewardCourses를 List<MemberReward>로 만듦
        List<MemberReward> registeredMemberRewards = registerableRewardCourses.stream()
                .map(course -> {
                    List<MemberReward.RewardSpotProgress> rewardSpotProgressList = course.getRewardSpots().stream()
                            .map(spot -> MemberReward.RewardSpotProgress.builder()
                                    .placeId(spot.getPlaceId()).build()).toList();

                    return MemberReward.builder()
                            .memberId(memberId)
                            .rewardCourseId(course.getId())
                            .spotProgressList(rewardSpotProgressList)
                            .build();
                }).toList();

        return memberRewardRepository.saveAll(registeredMemberRewards);
    }

    //URL 정합성 검사
    private boolean validateQrCodeIntegrity(String naegiftUrl, String placeId) {
        //URL 유효성 검사
        if (!validateQrUrlFormat(naegiftUrl)) {
            return false;
        }

        //naegiftId 추출
        String naegiftIdFromUrl = naegiftUrl.substring(naegiftUrl.lastIndexOf("/") + 1);

        //정합성: Place의 naegiftId와 naegiftURL의 식별자가 동일한지 검증
        //정합성 검증
        Optional<Place> targetPlace = placeRepository.findById(placeId);
        if (!targetPlace.isPresent()) {
            log.warn("[Reward] 내기프트 ID와 비교하기 위한 Place 데이터 없음");
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND, "QR코드로 들어온 내기프트에 해당하는 장소가 DB에 없음");
        }
        if (naegiftIdFromUrl.equals(targetPlace.get().getNaegiftId())) {
            return true;
        }
        return false;
    }

    //URL 유효성 검사
    private boolean validateQrUrlFormat(String naegiftUrl) {
        if (naegiftUrl == null || !naegiftUrl.matches(QR_URL_PATTERN)) {
            return false;
        }
        return true;
    }

    //사용자 이메일 + 리워드 코스 ID에 맞는 MemberReward 한개 가져오기
    private MemberReward findMemberRewardByEmailAndRewardCourseIdOrThrow(String identifier, String rewardCourseId) {
        //1. Email로 Member Id 가져오기
        String memberId = memberRepository.findMemberIdByEmailOrElseThrow(identifier);

        //2. Member Id와 RewardCourseId로 MemberReward 정보 한개 가져오기
        Optional<MemberReward> memberRewardOpt = memberRewardRepository.findByMemberIdAndRewardCourseId(memberId, rewardCourseId);
        if (!memberRewardOpt.isPresent()) {
            log.warn("[Reward] 해당 리워드 코스 진행 내역 조회 실패 (RewardCourseId: {})", rewardCourseId);
            throw new CustomException(ErrorCode.COURSE_NOT_FOUND, "사용자의 리워드 코스 진행 내역 조회를 실패하였습니다");
        }

        return memberRewardOpt.get();
    }

    private boolean validateProgressSpotsIntegrity(List<MemberReward.RewardSpotProgress> spotProgressList) {
        //검사: isVerified가 모두 Ture/verifiedAt이 모두 현재 시간보다 이전 시간/placeId로 검색한 place가 모두 place Collection에 있는지

        //검사1: isVerified == true
        //검사2: verifiedAt < now
        for (MemberReward.RewardSpotProgress spotProgress : spotProgressList) {
            if (!spotProgress.isVerified()) {
                log.info("[Reward] [Validate] 미인증 장소 존재 (PlaceId: {})", spotProgress.getPlaceId());
                return false;
            }

            if (spotProgress.getVerifiedAt().isAfter(LocalDateTime.now())) {
                log.warn("[Reward] [Validate] 완료/발급 시도 시간보다 장소 인증 시간이 미래임 (VerifiedAt: {})", spotProgress.getVerifiedAt());
                return false;
            }
        }

        //검사3: inputPlaceId에 해당하는 Place가 있는지 + naegiftId가 있는지
        List<String> placeIds = spotProgressList.stream()
                .map(MemberReward.RewardSpotProgress::getPlaceId)
                .toList();

        Map<String, Place> placeMap = placeRepository.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, place -> place));

        // 3. 누락된 장소가 있거나 내기프트 ID가 없는지 한 번만 순회하며 검증
        for (String id : placeIds) {
            Place place = placeMap.get(id);
            if (place == null) {
                log.warn("[Reward] [Validate] 방문한 장소가 Place 컬렉션에 없음 (PlaceId: {})", id);
                return false;
            }
            if (!StringUtils.hasText(place.getNaegiftId())) {
                log.warn("[Reward] [Validate] 장소의 naegiftId 누락 (PlaceId: {})", id);
                return false;
            }
        }
        return true;
    }

    private boolean validateMemberRewardIntegrity(MemberReward targetMemberReward) {
        //검사: isComplete가 모두 true/completeAt이 현재 시간보다 이전인지/rewardCourseId에 해당하는 리워드 코스가 있는지/memberId에 해당하는 유저가 있는지(+email 존재 여부도)

        boolean isCourseCompleted = targetMemberReward.isCompleted();
        if(!isCourseCompleted){
            log.warn("[Reward] [Validate] 달성을 하지 못함 (IsComplete: {})", isCourseCompleted);
            return false;
        }

        LocalDateTime completedTime = targetMemberReward.getCompletedAt();
        if(completedTime.isAfter(LocalDateTime.now())){
            log.warn("[Reward] [Validate] 코스 완료 시간이 리워드 발급 요청시간보다 미래 (CompletedAt: {})", completedTime);
            return false;
        }

        String targetRewardCourseId = targetMemberReward.getRewardCourseId();
        if(!rewardCourseRepository.existsById(targetRewardCourseId)){
            log.warn("[Reward] [Validate] 해당하는 리워드 코스가 없습니다.");
            return false;
        }

        String memberId = targetMemberReward.getMemberId();
        if (!memberRepository.existsByIdAndEmailIsNotNull(memberId)) {
            log.warn("[Reward] [Validate] 유저의 이메일 정보가 유실됨");
            return false;
        }
        return true;
    }
}
