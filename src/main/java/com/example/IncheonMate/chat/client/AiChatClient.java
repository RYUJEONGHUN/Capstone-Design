package com.example.IncheonMate.chat.client;

import com.example.IncheonMate.chat.dto.FastApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//+++++++++++++++++++++++++++++++++++++++나중에 URL 수정 필수+++++++++++++++++++++++++++++++++++++++++++++
@FeignClient(name = "ai-chat-client", url="http://localhost:8000/api/v1/ai")
public interface AiChatClient {

    //채팅
    @PostMapping
    FastApi.ChatResponseDto getAnswerMessage(@RequestBody FastApi.ChatRequestDto requestDto);

}
