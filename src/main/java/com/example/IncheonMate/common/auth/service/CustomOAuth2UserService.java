package com.example.IncheonMate.common.auth.service;

import com.example.IncheonMate.common.auth.dto.CustomOAuth2User;
import com.example.IncheonMate.member.domain.Member;
import com.example.IncheonMate.member.dto.MemberDto;
import com.example.IncheonMate.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 정보 추출-provicer에 따라서 다르게 추출/에러처리
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email;
        String name;
        if("google".equals(provider)){
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }
        else if("kakao".equals(provider)){
            Map<String, Object> kakaoAccount = (Map<String,Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String,Object>) kakaoAccount.get("profile");
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
        } else {
            email = "";
            name = "";
        }

        // 3. DB 저장 또는 업데이트 (MongoDB)
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 없으면 새로 가입
                    Member newMember = Member.builder()
                            .email(email)
                            .name(name)
                            .role("ROLE_USER")
                            .build();
                    return memberRepository.save(newMember);
                });

        // 4. MemberDto 생성
        MemberDto memberDto = new MemberDto();
        memberDto.setEmail(member.getEmail());
        memberDto.setName(member.getName());
        memberDto.setRole(member.getRole());

        // 5. CustomOAuth2User 반환 (이게 SuccessHandler로 넘어감)
        return new CustomOAuth2User(memberDto);
    }
}
