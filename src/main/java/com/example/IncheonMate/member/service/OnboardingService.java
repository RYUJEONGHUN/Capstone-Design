package com.example.IncheonMate.member.service;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.dto.OnboardingDto;
import com.example.IncheonMate.member.dto.SasangAnswerDto;
import com.example.IncheonMate.member.dto.SasangResultDto;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.member.domain.type.SasangType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;

    // 정규식 컴파일 최적화 (상수화)
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[A-Za-z0-9가-힣]{2,10}$");

    //checkNicknameAvailability 컨트롤러
    //닉네임 중복 및 정책 검사
    @Transactional(readOnly = true)
    public boolean isNicknameAvailability(String email, String nickname) {
        //정책 검사
        if (!checkNicknamePolicy(nickname)) {
            log.info("'{}' 닉네임 정책 위반", email);
            return false;
        }
        //중복 검사
        if (memberRepository.existsByNickname(nickname)) {
            log.info("'{}' 닉네임 중복", email);
            return false;
        }

        log.info("'{}' 닉네임 검사 통과", email);
        return true;
    }


    //닉네임 정책 검사
    //영어,한글,숫자만 포함하고 글자수는 2-10글자로 제한
    private boolean checkNicknamePolicy(String nickname) {
        //nickname이 null이면 NullPointerExecption(unchecked) 발생 -> null check 제일 앞에
        //띄어쓰기만 있는 빈 문자열도 허용하지 않음
        if (!StringUtils.hasText(nickname)) return false; // null 및 공백 체크 유틸 활용
        return NICKNAME_PATTERN.matcher(nickname).matches();
    }

    //setLanguage 컨트롤러
    //언어 설정 저장
    @Transactional
    public void setLanguage(String email, String language) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        member.updateLang(language);
        memberRepository.save(member);
        log.info("'{}' 언어 설정을 완료했습니다: {}", email, language);

    }

    //submitSasangTest컨트롤러
    //사상의학 테스트 결과 도출
    @Transactional
    public SasangResultDto deriveSasangResult(List<SasangAnswerDto> testResult, String email) {
        //체질 도출 로직
        SasangType sasangType = analyzeSasangType(testResult);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        member.updateSasang(sasangType);
        memberRepository.save(member);
        log.info("'{}' 체질 저장 완료: {}", email, sasangType);

        return SasangResultDto.builder()
                .sasangType(sasangType)
                .email(email)
                .build();
    }

    private SasangType analyzeSasangType(List<SasangAnswerDto> testResult) {
        // 🛡️ 안전장치: 문항 번호(questionId) 기준으로 오름차순 정렬-Gemini
        testResult.sort(Comparator.comparingInt(SasangAnswerDto::getQuestionId));

        //선택한 문항에 각 문항별로 가중치를 곱해서 태음,소음,소양,태양인을 선별함
        int[] sasangWeightResult = new int[5];
        //1,2,3,4 인덱스만으로 체질 판단하기 때문에 index0의 value는 반드시 1보다 낮아야함
        sasangWeightResult[0] = 0;
        //문항별 가중치 배열
        //공동 1등 방지를 위해서 비트 마스크 추가-Gemini
        //변경: (값 * 10000) + 2^index
        int[] eachQuestionsWeight = {
                870001, 790002, 730004, 590008, 990016,
                530032, 330064, 210128, 270256, 470512,
                411024, 932048, 674096
        };

        if (testResult.size() != eachQuestionsWeight.length) {
            log.error("답안 개수({}개)와 가중치 데이터 개수({}개)가 맞지 않습니다!", testResult.size(), eachQuestionsWeight.length);
            throw new IllegalArgumentException("답안 개수인 " + testResult.size() + "와 가중치 데이터 개수인 " + eachQuestionsWeight.length + "가 맞지 않습니다!");
        }
        //가중치 계산
        for (int i = 0; i < testResult.size(); i++) {
            switch (testResult.get(i).getAnswer()) {
                case 1:
                    sasangWeightResult[1] += eachQuestionsWeight[i];
                    break;
                case 2:
                    sasangWeightResult[2] += eachQuestionsWeight[i];
                    break;
                case 3:
                    sasangWeightResult[3] += eachQuestionsWeight[i];
                    break;
                case 4:
                    sasangWeightResult[4] += eachQuestionsWeight[i];
                    break;
            }
        }
        //가중치 합으로 체질 인덱스 도출
        int sasang = 1;
        for (int i = 2; i < sasangWeightResult.length; i++) {
            if (sasangWeightResult[sasang] < sasangWeightResult[i])
                sasang = i;
        }
        //체질 return
        SasangType sasangType;
        switch (sasang) {
            case 1:
                return SasangType.TAEUM;
            case 2:
                return SasangType.SOEUM;
            case 3:
                return SasangType.SOYANG;
            case 4:
                return SasangType.TAEYANG;
            default:
                throw new IllegalArgumentException("잘못된 체질 번호입니다.");
        }
    }

    @Transactional
    public void saveOnboarding(String email, @Valid OnboardingDto onboardingDto) {
        /*
        String nickname -> 최소 2글자/'사용자' 미포함
        String birthdate -> 6자리 숫자
        String mbti -> 대소문자 허용
        String profileImage; -> nullable
        CompanionType companion ->not null
        SasangType sasang -> not null
        String selectedPersonaId -> not blank,not null
        */
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));

        //기존 멤버에 온보딩 DTO를 반영함
        Member updatedMember = targetMember.toBuilder()
                .nickname(onboardingDto.getNickname())
                .birthDate(parseLocalDate(onboardingDto.getBirthDate()))
                .mbti(parseMbti(onboardingDto.getMbti()))
                .profileImageURL(onboardingDto.getProfileImageURL())
                .profileImageAsMarker(StringUtils.hasText(onboardingDto.getProfileImageURL()))
                .companion(onboardingDto.getCompanion())
                .sasang(onboardingDto.getSasang())
                .selectedPersonaId(onboardingDto.getSelectedPersonaId())
                .build();

        memberRepository.save(updatedMember);
    }

    private MbtiType parseMbti(String mbti) {
        return MbtiType.valueOf(mbti.toUpperCase());
    }

    private LocalDate parseLocalDate(String birthdate) { // 입력값: "990101"
        // 1. 문자열 자르기 (String -> int)
        int yearTwoDigit = Integer.parseInt(birthdate.substring(0, 2)); // 99
        int month = Integer.parseInt(birthdate.substring(2, 4));        // 01
        int day = Integer.parseInt(birthdate.substring(4, 6));          // 01

        // 2. 연도 보정 로직 (1900년 vs 2000년)
        // 현재 연도의 뒷자리(예: 25)를 구함
        int currentYearTwoDigit = LocalDate.now().getYear() % 100;

        int fullYear;
        // 입력된 연도(99)가 현재(25)보다 크면 -> 과거(1999년)
        // 입력된 연도(10)가 현재(25)보다 작으면 -> 최근(2010년)
        if (yearTwoDigit > currentYearTwoDigit) {
            fullYear = 1900 + yearTwoDigit;
        } else {
            fullYear = 2000 + yearTwoDigit;
        }

        // 3. LocalDate 객체 생성 (이게 yyyy-MM-dd 형식의 객체가 됨)
        return LocalDate.of(fullYear, month, day);
    }
}
