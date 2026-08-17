package com.example.cars.shared.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI carManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Car Management API")
                        .description("REST API for registering users and managing each user's cars. Built with Spring Boot and hexagonal architecture.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Car Management Team"))
                        .license(new License()
                                .name("Technical test project")))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local development server")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by POST /api/auth/login.")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
