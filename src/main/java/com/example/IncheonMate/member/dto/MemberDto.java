package com.example.IncheonMate.member.dto;

import lombok.Data;

@Data
//JWT
public class MemberDto {
    private String role;
    private String name;
    private String email;
}