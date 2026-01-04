package com.example.IncheonMate.persona.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL) //값이 null인 필드는 JSON에서 자동으로 사라짐-Gemini
public class PersonaDto {
    private String name;
    private String tags;
    private String overlayImageURL;
    private String mapImageURL;
    private String selectImageURL;
    private String chatImageURL;
}
