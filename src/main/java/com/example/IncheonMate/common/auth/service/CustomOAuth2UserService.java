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
        // 1. 구글에서 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 정보 추출 (구글 기준)
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

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
