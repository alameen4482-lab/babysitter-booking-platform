package com.babysitterbooking.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 3 configuration.
 *
 * <p>Exposes Swagger UI at {@code /swagger-ui.html} with:
 * <ul>
 *   <li>Project metadata (title, description, version)</li>
 *   <li>Bearer JWT security scheme so the "Authorize" button works</li>
 *   <li>Global security requirement applied to all secured endpoints</li>
 * </ul>
 *
 * <p>Access after starting the application:
 * <pre>http://localhost:8080/swagger-ui.html</pre>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Babysitter Booking Platform API")
                .description("""
                        **Capstone Project — Java Track**
                        
                        REST API for the Babysitter Booking Platform.
                        
                        **CSE/IT Specialization:** Concurrency Handling & Optimized DB Transactions.
                        
                        ### Authentication
                        All protected endpoints require a `Bearer` JWT token in the Authorization header.
                        Use the `/auth/login` endpoint to obtain a token, then click the **Authorize** button above.
                        
                        ### Roles
                        | Role | Description |
                        |------|-------------|
                        | `PARENT` | Books babysitters and submits reviews |
                        | `BABYSITTER` | Manages availability and responds to bookings |
                        | `ADMIN` | Full platform oversight |
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Capstone Team")
                        .email("capstone@babysitterbooking.com")
                )
                .license(new License()
                        .name("Academic — Capstone Project")
                );
    }

    private SecurityScheme jwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste your JWT token here (without the 'Bearer' prefix)");
    }
}
