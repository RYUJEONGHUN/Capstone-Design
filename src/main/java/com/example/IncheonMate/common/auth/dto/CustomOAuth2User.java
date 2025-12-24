package com.example.IncheonMate.common.auth.dto;

import com.example.IncheonMate.member.dto.MemberDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final MemberDto memberDto;

    public CustomOAuth2User(MemberDto memberDto) {
        this.memberDto = memberDto;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null; // JWT 방식이라 사용 안 함
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return memberDto.getRole();
            }
        });
        return collection;
    }

    @Override
    public String getName() {
        return memberDto.getName();
    }

    // 우리가 추가로 쓰는 메서드 (이메일 꺼낼 때 필요)
    public String getEmail() {
        return memberDto.getEmail();
    }
}