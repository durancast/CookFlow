package com.cookflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI cookflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CookFlow API")
                        .description("TPV para restaurante - Spring Boot 3 + PostgreSQL (multi-tenant por tenant_id)")
                        .version("0.1.0"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .schemaRequirement(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
