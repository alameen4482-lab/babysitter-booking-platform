package com.babysitterbooking.config;

import com.babysitterbooking.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Spring Security configuration for the Babysitter Booking Platform.
 *
 * <p>Configuration highlights:
 * <ul>
 *   <li>Stateless sessions (JWT-based, no HTTP session)</li>
 *   <li>CSRF disabled (REST API, no browser form submissions)</li>
 *   <li>JWT filter injected before Spring's UsernamePasswordAuthenticationFilter</li>
 *   <li>Method-level security enabled ({@code @PreAuthorize} support)</li>
 *   <li>Custom 401 entry point returning the standard ApiResponse JSON</li>
 * </ul>
 *
 * <p>Public routes (no authentication required):
 * <ul>
 *   <li>POST /auth/register</li>
 *   <li>POST /auth/login</li>
 *   <li>GET  /actuator/health (if enabled)</li>
 *   <li>GET  /swagger-ui/** and /v3/api-docs/** (Swagger UI)</li>
 * </ul>
 *
 * <p>All other routes require a valid JWT.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final ObjectMapper objectMapper;

    /**
     * Public endpoints that do not require authentication.
     * Note: context-path /api is set in application.properties,
     * so these paths are relative to /api/.
     */
    private static final String[] PUBLIC_POST_ENDPOINTS = {
        "/auth/register",
        "/auth/login"
    };

    private static final String[] SWAGGER_WHITELIST = {
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/v3/api-docs"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — JWT handles state
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Swagger UI — publicly accessible
                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINTS).permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Custom 401 response using ApiResponse format
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(this::handleAuthenticationException)
            )

            // Set our custom AuthenticationProvider
            .authenticationProvider(authenticationProvider)

            // Add JWT filter before the standard username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Writes a standard {@link ApiResponse} JSON body for unauthenticated requests
     * instead of Spring Security's default HTML error page.
     */
    private void handleAuthenticationException(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(
            "Authentication required. Please provide a valid Bearer token."
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
