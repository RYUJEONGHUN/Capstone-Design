package com.example.IncheonMate.member.service;

import com.example.IncheonMate.member.dto.SasangQuestionDto;
import com.example.IncheonMate.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private List<SasangQuestionDto> korSasangQuestionDtoList;
    private List<SasangQuestionDto> engSasangQuestionDtoList;

    //사상의학 한글 설문 로딩
    @PostConstruct
    public void initKorSasangData() {
        try {
            // 파일을 읽어서 리스트로 변환
            ClassPathResource resource = new ClassPathResource("data/sasangQuestion_kor.json");

            // JSON -> Java Object 변환 (마법 같은 한 줄)
            this.korSasangQuestionDtoList = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<SasangQuestionDto>>() {}
            );

            log.info("한국어 사상의학 질문 데이터 로딩 완료! (총 {}개)", korSasangQuestionDtoList.size());

        } catch (IOException e) {
            log.error("한국어 사상의학 데이터 파일 읽기 실패", e);
            throw new RuntimeException("한국어 사상의학 데이터 로딩 실패"); // 서버 시작 못 하게 막음
        }
    }

    //사상의학 영어 설문 로딩
    @PostConstruct
    public void initEngSasangData() {
        try {
            // 파일을 읽어서 리스트로 변환
            ClassPathResource resource = new ClassPathResource("data/sasangQuestion_eng.json");

            // JSON -> Java Object 변환 (마법 같은 한 줄)
            this.engSasangQuestionDtoList = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<SasangQuestionDto>>() {}
            );

            log.info("영어 사상의학 질문 데이터 로딩 완료! (총 {}개)", engSasangQuestionDtoList.size());

        } catch (IOException e) {
            log.error("영어 사상의학 데이터 파일 읽기 실패", e);
            throw new RuntimeException("영어 사상의학 데이터 로딩 실패"); // 서버 시작 못 하게 막음
        }
    }


    //checkNicknameAvailability 컨트롤러
    //닉네임 중복 및 정책 검사
    public boolean isNicknameAvailability(String email, String nickname) {
        //정책 검사
        if(!checkNicknamePolicy(nickname)) {
            log.info("'{}' 닉네임 정책 위반",email);
            return false;
        }
        //중복 검사
        if(memberRepository.existsByNickname(nickname)){
            log.info("'{}' 닉네임 중복",email);
            return false;
        }

        log.info("'{}' 닉네임 검사 통과",email);
        return true;
    }


    //getSasangTestQuestions 컨트롤러
    public List<SasangQuestionDto> getSasangQuestions(String email){
        String lang = memberRepository.findLangByEmail(email);
        
        //lang.equals("kor)을 사용하면 호출 대상인 lang이 null일때 null예외가 발생할 수 있음
        if("kor".equals(lang)) {
            log.info("'{}' 한국어 사상의학 설문 불러오기 성공",email);
            return korSasangQuestionDtoList;
        }
        else if("eng".equals(lang)){
            log.info("'{}' 영어 사상의학 설문 불러오기 성공",email);
            return engSasangQuestionDtoList;
        }
        else {
            log.error("'{}' 사상의학 설문을 불러오는데 실패했습니다!",email);
            throw new RuntimeException("사상의학 설문 불러오기 실패");
        }

    }


    //닉네임 정책 검사
    //영어,한글,숫자만 포함하고 글자수는 2-10글자로 제한
    private boolean checkNicknamePolicy(String nickname){
        //nickname이 null이면 NullPointerExecption(unchecked) 발생 -> null check 제일 앞에
        //띄어쓰기만 있는 빈 문자열도 허용하지 않음
        if(nickname == null || nickname.trim().isEmpty())
            return false;
        if(!nickname.matches("^[A-Za-z0-9가-힣]{2,10}$"))
            return false;

        return true;
    }
}
