package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.domain.type.SasangType;
import lombok.Builder;
import lombok.Data;

//선택한 옵션으로 4체질 중 하나 돌려주는 DTO
@Data
@Builder(toBuilder = true)
public class SasangResultDto {
    private String email;
    private SasangType sasangType;
}
