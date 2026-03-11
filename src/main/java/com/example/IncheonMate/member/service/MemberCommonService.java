package com.example.IncheonMate.member.service;

import com.example.IncheonMate.common.exception.CustomException;
import com.example.IncheonMate.common.exception.ErrorCode;
import com.example.IncheonMate.member.domain.type.MbtiType;
import com.example.IncheonMate.member.domain.type.SasangType;
import com.example.IncheonMate.member.dto.MemberCommonDto;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.repository.config.CustomRepositoryImplementationDetector;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
@Slf4j
public class MemberCommonService { //memberService와 onboardingService에서 공통 기능만 빼와서 2개의 서비스에 사용하는 용도 

    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    // 한글, 영문, 숫자, 공백 포함 2~10자-Gemini
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9\\s]{2,10}$");

    //닉네임 중복 및 정책 검사
    @Transactional(readOnly = true)
    public MemberCommonDto.NicknamePolicyDto isNicknameAvailability(String email, String nickname) {
        //정책 검사
        if (!checkNicknamePolicy(nickname)) {
            log.info("'{}' 닉네임 정책 위반", email);
            return MemberCommonDto.NicknamePolicyDto.of(false,"닉네임 정책 위반: " + nickname);
        }
        //중복 검사
        //게스트 닉네임 중복 검사
        //게스트 많아지면 Redis Sets에 닉네임만 따로 저장해놓아야 성능 저하 없음(Redis는 Single-thread임)
        Set<String> redisKeys = redisTemplate.keys("GUEST_PROFILE:*");
        boolean hasSameNickname = redisKeys.stream()
                .map(key -> (String) redisTemplate.opsForHash().get(key, "nickname"))
                .filter(Objects::nonNull)
                .anyMatch(redisNickname -> redisNickname.equals(nickname));
        if(hasSameNickname){
            log.info("'{}' 닉네임 중복", email);
            return MemberCommonDto.NicknamePolicyDto.of(false,"닉네임 중복: " + nickname);
        }

        //유저 닉네임 중복 검사
        if (memberRepository.existsByNickname(nickname)) {
            log.info("'{}' 닉네임 중복", email);
            return MemberCommonDto.NicknamePolicyDto.of(false,"닉네임 중복: " + nickname);
        }

        log.info("'{}' 닉네임 검사 통과", email);
        return MemberCommonDto.NicknamePolicyDto.of(true,"닉네임 검사 통과: " + nickname);
    }


    //닉네임 정책 검사
    //영어,한글,숫자만 포함하고 글자수는 2-10글자로 제한
    public boolean checkNicknamePolicy(String nickname) {
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


    public SasangType analyzeSasangType(List<MemberCommonDto.SasangAnswerDto> testResult) {
        // 🛡️ 안전장치: 문항 번호(questionId) 기준으로 오름차순 정렬-Gemini
        testResult.sort(Comparator.comparingInt(MemberCommonDto.SasangAnswerDto::questionId));

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
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,"답안 개수인 " + testResult.size() + "와 가중치 데이터 개수인 " + eachQuestionsWeight.length + "가 맞지 않습니다!");
        }
        //가중치 계산
        for (int i = 0; i < testResult.size(); i++) {
            switch (testResult.get(i).answer()) {
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
                log.warn("체질 결과를 도출 할 수 없습니다: {}", sasang);
                throw new CustomException(ErrorCode.INVALID_SASANG_TYPE);
        }
    }

    //yyMMdd를 yyyy-MM-dd형식으로 변경
    public LocalDate parseLocalDate(String birthdate) {
        try {
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
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "미래 날짜는 생년월일로 설정할 수 없습니다.");
            }

            return result;
        } catch (NumberFormatException | DateTimeException e){
            log.warn("생년월일 파싱 실패: {}", birthdate);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "올바르지 않은 생년월일 형식입니다.");
        }
    }

    //MBTI가 대문자,소문자 어떤 형식으로 들어오던 간에 항상 전부 대문자로 저장하게 toUpperCase하는 로직
    public MbtiType parseMbti(String mbti) {
        try {
            return MbtiType.valueOf(mbti.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 MBTI 입력: {}", mbti);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 MBTI 값입니다: " + mbti);
        }
    }
}
