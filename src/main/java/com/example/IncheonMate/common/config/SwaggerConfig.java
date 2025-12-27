package com.example.IncheonMate.common.config; // 패키지명은 본인 프로젝트에 맞게 수정

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 1. JWT 토큰 설정 (Authorize 버튼 생성)
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

        // 2. Security Scheme 등록
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT") // 토큰 형식을 명시
        );

        // 3. API 정보 설정
        return new OpenAPI()
                .components(components)
                .addSecurityItem(securityRequirement)
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("IncheonMate API Document") // API 제목
                .description("인천메이트 프로젝트 API 명세서입니다.") // 설명
                .version("1.0.0"); // 버전
    }
}