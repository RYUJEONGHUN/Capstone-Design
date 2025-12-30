package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.SasangType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SasangResultDto {
    String eamil;
    SasangType sasangType;
}
