package com.babysitterbooking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring context integration test.
 *
 * <p>Verifies that the Spring application context loads successfully
 * with all beans (Security, JPA, JwtUtil, OpenAPI, etc.) wired correctly.
 *
 * <p>Uses the test profile which activates H2 in-memory database
 * (see {@code src/test/resources/application.properties}) so no MySQL
 * instance is required for the build to pass.
 */
@SpringBootTest
class BabysitterBookingApplicationTests {

    /**
     * Confirms the Spring context loads without errors.
     * If this test passes, the application foundation is correctly configured.
     */
    @Test
    void contextLoads() {
        // No assertions needed — test passes if context starts without throwing
    }
}
