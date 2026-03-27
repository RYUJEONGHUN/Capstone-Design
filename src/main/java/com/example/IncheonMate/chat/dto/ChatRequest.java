package com.example.IncheonMate.chat.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    public record MessageDto(
            @NotBlank String message
    ){}
}
