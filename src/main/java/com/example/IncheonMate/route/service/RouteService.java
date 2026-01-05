package com.example.IncheonMate.route.service;

import com.example.IncheonMate.route.client.OdsayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    private final OdsayClient odsayClient;

    @Value("${ODSAY_KEY}")
    private String apiKey;

    public Map<String, Object> getRoute(){
        log.info("요청함");
        Map<String, Object> response = odsayClient.searchRoute(
                "126.9027279","37.5349277",
                "126.9145430","37.5499421",
                apiKey);

        return response;
    }
}
