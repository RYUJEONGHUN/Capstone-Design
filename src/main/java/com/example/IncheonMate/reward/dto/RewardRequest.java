package com.example.IncheonMate.reward.dto;

public class RewardRequest {

    public record VerifySpotRequest(
            String qrCodeUrl
    ){}
}
