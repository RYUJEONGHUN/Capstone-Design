package com.example.IncheonMate.member.service;

import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.dto.OnboardingDto;
import com.example.IncheonMate.member.dto.SasangAnswerDto;
import com.example.IncheonMate.member.dto.TermsAgreementDto;
import com.example.IncheonMate.member.repository.MemberRepository;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.example.IncheonMate.persona.repository.PersonaRepository;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final PersonaRepository personaRepository;

    // 한글, 영문, 숫자, 공백 포함 2~10자-Gemini
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9\\s]{2,10}$");

    //현재 약관 버전
    private static final String CURRENT_TERMS_VERSION = "v1.0.0";

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


    public record SasangResultResponse(String eamil,SasangType sasangType){}
    //submitSasangTest컨트롤러
    //사상의학 테스트 결과 도출
    public SasangResultResponse deriveSasangResult(List<SasangAnswerDto> testResult, String email) {
        //체질 도출 로직
        SasangType sasangType = analyzeSasangType(testResult);

        log.info("'{}' 사상의학 테스트 결과: {}", email, sasangType);

        return new SasangResultResponse(email,sasangType);
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
        lang -> kor or eng
        */
        //온보딩DTO null 검증
        if (onboardingDto == null) {
            throw new IllegalArgumentException("온보딩 데이터가 없습니다.");
        }

        //저장할 멤버
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));

        //페르소나ID가 컬렉션에 있는것과 맞는지 검증
        String validatedPersonaId = validatePersoanId(onboardingDto.getSelectedPersonaId());
        //닉네임이 정책에 맞게 들어왔는지 검증
        if (!checkNicknamePolicy(onboardingDto.getNickname())) {
            throw new IllegalArgumentException("닉네임 정책에 맞지 않습니다: " + onboardingDto.getNickname());
        }
        //yyMMdd형식을 yyyy-MM-dd형식으로 변환하고 미래의 날짜인지 검증
        LocalDate birthDate = parseLocalDate(onboardingDto.getBirthDate());

        //기존 멤버에 온보딩 DTO를 반영함
        //생년월일-현재보다 미래의 날짜도 통과하는 문제 => isAfter()로 해결
        //profileImageURL-null이면 exception나오는 문제 => 반드시 profileImageURL: null 형태로 전달받아야함(없으면 exception)
        //selectedPersonaId-컬렉션에 있는 personaId와 달라도 통과하는 문제 => 해결(vaildatePersoanId)
        Member updateMember = targetMember.toBuilder()
                .nickname(onboardingDto.getNickname())
                .birthDate(birthDate)
                .mbti(parseMbti(onboardingDto.getMbti()))
                .profileImageURL(onboardingDto.getProfileImageURL())
                .profileImageAsMarker(StringUtils.hasText(onboardingDto.getProfileImageURL()))
                .companion(onboardingDto.getCompanion())
                .sasang(onboardingDto.getSasang())
                .selectedPersonaId(validatedPersonaId)
                .lang(onboardingDto.getLang())
                .build();

        memberRepository.save(updateMember);
    }

    private String validatePersoanId(String selectedPersonaId) {
        if (!personaRepository.existsById(selectedPersonaId)) {
            log.error("({})에 해당하는 페르소나ID가 없습니다.", selectedPersonaId);
            throw new NoSuchElementException("(" + selectedPersonaId + ")에 해당하는 페르소나ID가 없습니다.");
        }
        return selectedPersonaId;
    }

    private MbtiType parseMbti(String mbti) {
        return MbtiType.valueOf(mbti.toUpperCase());
    }

    //yyMMdd를 yyyy-MM-dd형식으로 변경
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

    //일회성인 약관 응답을 위한 java record(java 16이상)
    public record AgreementResponse(String email, LocalDateTime agreedAt, String version) {}

    //saveAgreements컨트롤러
    @Transactional
    public AgreementResponse saveAgreements(String email, TermsAgreementDto termsAgreementDto) {
        //저장할 멤버
        Member targetMember = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("해당 이메일을 가진 멤버를 찾을 수 없습니다: " + email));
        //현재 시간
        LocalDateTime now = LocalDateTime.now();
        //약관 버전->버전관리 필요하면 메소드 형태로 변형
        String currentTermsVersion = CURRENT_TERMS_VERSION;

        //멤버 약관 동의 저장
        memberRepository.save(targetMember.toBuilder()
                .isPrivacyPolicyAgreed(termsAgreementDto.isPrivacyPolicyAgreed())
                .isLocationServiceAgreed(termsAgreementDto.isLocationServiceAgreed())
                .isTermsOfServiceAgreed(termsAgreementDto.isTermsOfServiceAgreed())
                .allTermsAgreedAt(now)
                .termsVersion(currentTermsVersion)
                .build());
        log.info("'{}' 약관 동의 내역 저장 완료",email);
        return new AgreementResponse(email, now, currentTermsVersion);
    }

}
