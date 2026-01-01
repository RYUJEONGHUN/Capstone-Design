package com.example.IncheonMate.member.service;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.example.IncheonMate.member.dto.MyInfoResponse;
import com.example.IncheonMate.member.dto.SasangAnswerDto;
import com.example.IncheonMate.member.dto.SasangResultDto;
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
import java.util.regex.Pattern;

import static com.example.IncheonMate.member.service.OnboardingService.analyzeSasangType;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // 한글, 영문, 숫자, 공백 포함 2~10자-Gemini
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9\\s]{2,10}$");

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

    @Transactional(readOnly = true)
    public MyInfoResponse.NicknamePolicyDto isNicknameAvailability(String email, String nickname) {
        //정책 검사
        if (!checkNicknamePolicy(nickname)) {
            log.info("'{}' 닉네임 정책 위반", email);
            return MyInfoResponse.NicknamePolicyDto.from(false);
        }
        //중복 검사
        if (memberRepository.existsByNickname(nickname)) {
            log.info("'{}' 닉네임 중복", email);
            return MyInfoResponse.NicknamePolicyDto.from(false);
        }

        log.info("'{}' 닉네임 검사 통과", email);
        return MyInfoResponse.NicknamePolicyDto.from(true);
    }

    //닉네임 정책 검사
    //영어,한글,숫자만 포함하고 글자수는 2-10글자로 제한
    private boolean checkNicknamePolicy(String nickname) {
        //nickname이 null이면 NullPointerExecption(unchecked) 발생 -> null check 제일 앞에
        //띄어쓰기만 있는 빈 문자열도 허용하지 않음
        if (!StringUtils.hasText(nickname)) {
            log.info("null이거나 공백인 닉네임입니다: {}", nickname);
            return false; // null 및 공백 체크 유틸 활용
        }
        String cleanNickname = nickname.replace(" ", "");
        if (cleanNickname.contains("사용자")) {
            log.info("금칙어(사용자)가 포함된 닉네임입니다: {}", nickname);
            return false;
        }

        return NICKNAME_PATTERN.matcher(nickname).matches();
    }

    //onboarding analyzeSasangType static으로 재사용
    @Transactional
    public SasangResultDto deriveSasangResult(String email, List<SasangAnswerDto> sasangAnswerDtos) {
        //체질 도출 로직
        SasangType sasangType = analyzeSasangType(sasangAnswerDtos);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        //사상 체질 결과 저장
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        memberRepository.save(
                targetMember.toBuilder()
                        .sasang(sasangType)
                        .build());

        return new SasangResultDto(email, sasangType);

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

        if (!checkNicknamePolicy(myProfileDto.nickname())) {
            throw new IllegalArgumentException("닉네임 정책에 맞지 않습니다: " + myProfileDto.nickname());
        }

        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = parseLocalDate(myProfileDto.birthdate());

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

    private LocalDate parseLocalDate(String birthdate) {
        int yearTwoDigit = Integer.parseInt(birthdate.substring(0, 2));
        int month = Integer.parseInt(birthdate.substring(2, 4));
        int day = Integer.parseInt(birthdate.substring(4, 6));

        int currentYear = LocalDate.now().getYear();
        int currentYearTwoDigit = currentYear % 100;

        // 1. 우선 2000년대라고 가정
        int fullYear = 2000 + yearTwoDigit;

        // 2. 만약 계산된 연도가 내년(올해+1)보다 크다면, 1900년대일 확률이 높음
        // 1년의 여유를 두는 이유는 '26'이 내년(미래 오타)인지 '1926'인지 구분하기 위함
        if (fullYear > currentYear + 1) {
            fullYear -= 100;
        }

        // 3. 실제 날짜 객체 생성 (존재하지 않는 날짜면 예외 발생)
        LocalDate result = LocalDate.of(fullYear, month, day);

        // 4. 최종 미래 날짜 검증 (이제 '26'이 2026년으로 유지되어 여기서 걸러짐)
        if (result.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래 날짜는 생년월일로 설정할 수 없습니다.");
        }

        return result;
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
