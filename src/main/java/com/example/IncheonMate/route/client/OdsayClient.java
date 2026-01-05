package com.example.IncheonMate.route.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
/*
Spring boot: 요청 하나당 스레스 하나 할당하는 Thread Per Request 사용
톰캣 기본 설정: 최대 스레드 200개
사용자가 길찾기 요청하면 한개의 스레드에 요청을 할당한다.->이때 비동기와 동기의 차이가 생김
openfeign: 동기,스레드는 요청이 끝날때까지 기다림
webclient: 비동기,스레드가 요청이 끝날때까지 기다리지 않음

odsay의 응답이 1초 걸린다고 가정할때 2000-3000명까지는 원할함
감당 불가능 할때: scale-out -> 비동기(webclinet)전환
 */
@FeignClient(name="odsayClient", url = "https://api.odsay.com/v1/api")
public interface OdsayClient {

    //길찾기 Get요청
    @GetMapping("/searchPubTransPathT")
    Map<String, Object> searchRoute(
            @RequestParam("SX") String sx,
            @RequestParam("SY") String sy,
            @RequestParam("EX") String ex,
            @RequestParam("EY") String ey,
            @RequestParam("apiKey") String apiKey
    );
}
