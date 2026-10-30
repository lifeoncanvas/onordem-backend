package com.honouryourmentor.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.domain:onodem.com}")
    private String appDomain;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        Server prodApiServer = new Server()
                .url("https://api." + appDomain)
                .description("Production API Server (" + appDomain + ")");

        Server prodServer = new Server()
                .url("https://" + appDomain)
                .description("Production Domain Server (" + appDomain + ")");

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");

        return new OpenAPI()
                .info(new Info()
                        .title("Honour Your Mentor API (onodemBackend)")
                        .version("1.0.0")
                        .description("REST API backend for Honour Your Mentor mobile and web application on " + appDomain)
                        .contact(new Contact().name("Honour Your Mentor").url("https://" + appDomain)))
                .servers(List.of(prodApiServer, prodServer, localServer))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
