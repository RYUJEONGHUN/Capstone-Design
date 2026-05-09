package com.example.IncheonMate.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    @Schema(name = "ChatMessageRequest")
    public record MessageDto(
            @NotBlank String message
    ){}
}
