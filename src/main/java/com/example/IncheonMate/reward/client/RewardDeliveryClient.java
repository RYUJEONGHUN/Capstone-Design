package com.example.IncheonMate.reward.client;

import com.example.IncheonMate.reward.dto.Naegift;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reward-delivery", url = "https://${app.naegift.url}/api/partners/incheonmate/rewards/claim")
public interface RewardDeliveryClient {

    @PostMapping("/api/partners/incheonmate/rewards/claim")
    Naegift.SuccessResponseDto deliverReward(@RequestBody Naegift.RequestDto requestDto);
}
