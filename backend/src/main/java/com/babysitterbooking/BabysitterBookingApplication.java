package com.babysitterbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Babysitter Booking Platform — Spring Boot Entry Point.
 *
 * <p>Capstone Project | Java Track | Spring Boot 3.x
 * <p>CSE/IT Specialization: Concurrency Handling & Optimized DB Transactions
 *
 * <p>Features enabled at application level:
 * <ul>
 *   <li>JPA Auditing — populates createdAt / updatedAt fields automatically</li>
 * </ul>
 */
@SpringBootApplication
@EnableJpaAuditing
public class BabysitterBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BabysitterBookingApplication.class, args);
    }
}
