package com.akkorhotel.hotel.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Akkor Hotel API")
                        .description("API for managing hotel bookings on Akkor Hotel")
                        .license(new License().name("Apache License Version 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                        .version("1.0.0"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("https://akkorhotel-api.up.railway.app")
                        .description("Production Server"))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("http://localhost:8080")
                        .description("Local Server"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .addTagsItem(new Tag().name("Authentication").description("Endpoints for user authentication and session management"))
                .addTagsItem(new Tag().name("User").description("Endpoints for user account management"))
                .addTagsItem(new Tag().name("Hotel").description("Endpoints for hotel management (creation, update, deletion, search)"))
                .addTagsItem(new Tag().name("Booking").description("Endpoints for hotel booking management"))
                .addTagsItem(new Tag().name("Admin").description("Endpoints for administrator-specific operations"));
    }
}
