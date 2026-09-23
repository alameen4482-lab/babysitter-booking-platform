package com.babysitterbooking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-level bean configuration.
 *
 * <p>Provides the BCrypt password encoder and exposes Spring Security's
 * auto-configured AuthenticationManager, which uses the application's
 * UserDetailsService and PasswordEncoder.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    /**
     * BCrypt password encoder.
     * Strength 12 is intentionally used for password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Exposes Spring Security's AuthenticationManager so the AuthService
     * can authenticate login requests programmatically.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
