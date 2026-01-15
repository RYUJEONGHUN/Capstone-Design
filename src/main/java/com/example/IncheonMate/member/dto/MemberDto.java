package com.example.IncheonMate.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//JWT
@Schema(description = "Member 기본 정보,인증/인가 DTO")
public class MemberDto {
    private String role;
    private String name;
    private String email;
}