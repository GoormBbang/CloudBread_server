package com.cloudbread.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server-url:}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI openAPI(){
        // API 기본 설정
        Info info = new Info()
                .title("구름빵 API Document")
                .version("1.0")
                .description(
                        "환영합니다! [구름빵]은 임산부용 음식/영양제 추천 플랫폼을 개발합니다. 이 API 문서는 구름빵의 API를 사용하는 방법을 설명합니다.\n"
                );

        // JWT 인증 방식 설정
        String jwtSchemeName = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER));


        OpenAPI api = new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);

        // ★ 배포/로컬별 서버 URL 주입 (비어있으면 추가하지 않음 = 현재 호스트 사용)
        if (swaggerServerUrl != null && !swaggerServerUrl.isBlank()) {
            api.addServersItem(new Server().url(swaggerServerUrl));
        }

        return api;
    }

}
