package com.nus.cs5224.smartsg.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("SmartSG API")
                                                .version("1.0")
                                                .description("SmartSG Backend API - JWT Authentication"))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                                .components(new Components()
                                                .addSecuritySchemes("Bearer Token",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Paste your JWT token here (without 'Bearer ' prefix)")));
        }
}
