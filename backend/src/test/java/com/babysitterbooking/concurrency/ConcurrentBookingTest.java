package com.babysitterbooking.concurrency;

import com.babysitterbooking.dto.BookingResponse;
import com.babysitterbooking.dto.CreateBookingRequest;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BabysitterRepository;
import com.babysitterbooking.repository.BookingAuditLogRepository;
import com.babysitterbooking.repository.BookingRepository;
import com.babysitterbooking.repository.UserRepository;
import com.babysitterbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency integration test verifying that simultaneous booking attempts
 * on the exact same AvailabilitySlot are safely handled.
 *
 * <p>Uses pessimistic locking with {@code LockModeType.PESSIMISTIC_WRITE}.
 * Guarantees that exactly one parent succeeds and the competing parent receives
 * a {@link ConflictException} (HTTP 409).
 */
@SpringBootTest
class ConcurrentBookingTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BabysitterRepository babysitterRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingAuditLogRepository bookingAuditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Babysitter babysitter;
    private AvailabilitySlot sharedSlot;

    @BeforeEach
    void setUp() {
        bookingAuditLogRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilitySlotRepository.deleteAll();
        babysitterRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Parent A
        User parentA = User.builder()
                .name("Parent Alpha")
                .email("alpha@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.PARENT)
                .build();
        userRepository.save(parentA);

        // Setup Parent B
        User parentB = User.builder()
                .name("Parent Beta")
                .email("beta@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.PARENT)
                .build();
        userRepository.save(parentB);

        // Setup Babysitter
        User sitterUser = User.builder()
                .name("Sitter Sarah")
                .email("sarah@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.BABYSITTER)
                .build();
        userRepository.save(sitterUser);

        babysitter = Babysitter.builder()
                .user(sitterUser)
                .hourlyRate(20.0)
                .experience("6 years")
                .skills("Infant Care, First Aid")
                .available(true)
                .build();
        babysitterRepository.save(babysitter);

        // Setup Shared Availability Slot
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(2);
        sharedSlot = AvailabilitySlot.builder()
                .babysitter(babysitter)
                .startTime(start)
                .endTime(end)
                .isBooked(false)
                .build();
        sharedSlot = availabilitySlotRepository.save(sharedSlot);
    }

    @Test
    @DisplayName("Concurrency: Simultaneous booking attempts on same slot result in exactly 1 success and 1 conflict")
    void concurrentBookingAttempts_OnlyOneSucceeds() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicReference<Exception> otherException = new AtomicReference<>();

        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(babysitter.getId())
                .slotId(sharedSlot.getId())
                .notes("Concurrent booking test")
                .build();

        String[] parents = {"alpha@test.com", "beta@test.com"};

        for (int i = 0; i < threadCount; i++) {
            final String parentEmail = parents[i];
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    // Wait for both threads to be ready so they strike simultaneously
                    startLatch.await();

                    BookingResponse response = bookingService.createBooking(request, parentEmail);
                    if (response != null && response.getId() != null) {
                        successCount.incrementAndGet();
                    }
                } catch (ConflictException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    otherException.set(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Wait until both threads are aligned
        readyLatch.await(5, TimeUnit.SECONDS);

        // Fire both threads simultaneously
        startLatch.countDown();

        // Wait for both threads to finish
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        assertThat(completed).isTrue();
        assertThat(otherException.get()).isNull();

        // Exactly one should succeed and one should receive ConflictException
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);

        // Database assertions: exactly 1 booking persisted
        assertThat(bookingRepository.count()).isEqualTo(1);

        // Database assertions: slot is booked
        AvailabilitySlot finalSlot = availabilitySlotRepository.findById(sharedSlot.getId()).orElseThrow();
        assertThat(finalSlot.getIsBooked()).isTrue();

        // Database assertions: exactly 1 PENDING audit log entry
        assertThat(bookingAuditLogRepository.count()).isEqualTo(1);
    }
}
