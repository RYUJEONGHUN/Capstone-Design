package com.example.IncheonMate.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class MDCLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //요청 자체에 대한 고유 ID 발급
        String traceId = UUID.randomUUID().toString().substring(0,8);
        MDC.put("traceId",traceId);

        MDC.put("userId","Anonymous");

        try{
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
