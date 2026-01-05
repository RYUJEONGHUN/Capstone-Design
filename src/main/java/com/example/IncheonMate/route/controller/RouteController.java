package com.example.IncheonMate.route.controller;

import com.example.IncheonMate.route.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping
    public ResponseEntity<Map<String ,Object>> getRoute() {
        return ResponseEntity.ok(routeService.getRoute());
    }
}
