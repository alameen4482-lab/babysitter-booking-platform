package com.babysitterbooking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-level bean configuration.
 *
 * <p>Provides:
 * <ul>
 *   <li>{@link PasswordEncoder} — BCrypt (strength 12) for password hashing</li>
 *   <li>{@link AuthenticationProvider} — DAO-based provider using the platform's
 *       {@link UserDetailsService} and {@link PasswordEncoder}</li>
 *   <li>{@link AuthenticationManager} — delegates to the auto-configured
 *       {@link AuthenticationConfiguration}</li>
 * </ul>
 *
 * <p>The {@link UserDetailsService} bean itself is defined in the
 * {@code UserService} class (to be created in the user module) and injected here
 * by Spring. This avoids a circular dependency between Security and Service layers.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final UserDetailsService userDetailsService;

    /**
     * BCrypt password encoder.
     * Strength 12 is recommended for production (default is 10).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DAO-based AuthenticationProvider that wires the custom UserDetailsService
     * and BCrypt encoder. Spring Security uses this to authenticate login requests.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager exposed as a Bean so it can be injected into
     * the Auth controller / service to programmatically authenticate users.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfiguration
    ) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }
}
