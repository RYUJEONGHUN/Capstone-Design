package com.example.IncheonMate.member.dto;

import com.example.IncheonMate.member.type.SasangType;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.List;

@Data
public class SasangQuestionDto {
    private int id;
    private String question;
    private List<Optoins> options;

    @Data
    @AllArgsConstructor
    public static class Optoins{
        private int optionId;
        private String text;
    }
}
