package com.babysitterbooking.service;

import com.babysitterbooking.dto.BookingResponse;
import com.babysitterbooking.dto.CreateBookingRequest;
import com.babysitterbooking.exception.ApiException;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.exception.ResourceNotFoundException;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BookingRepository;
import com.babysitterbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final UserRepository userRepository;
    private final BookingAuditLogService bookingAuditLogService;

    /**
     * Atomically reserves an availability slot and creates a new booking.
     *
     * <p>Concurrency Control:
     * Uses pessimistic write locking (SELECT ... FOR UPDATE) to lock the slot row.
     * Re-verifies {@code isBooked} inside the locked transaction so concurrent
     * attempts receive HTTP 409 Conflict.
     */
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String parentEmail) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", parentEmail));

        if (parent.getRole() != User.Role.PARENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only users with role PARENT can create bookings");
        }

        // Critical Section: Acquire exclusive row lock on the AvailabilitySlot
        AvailabilitySlot slot = availabilitySlotRepository.findByIdWithPessimisticLock(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("AvailabilitySlot", request.getSlotId()));

        // Validate that the slot belongs to the requested babysitter
        if (slot.getBabysitter() == null || slot.getBabysitter().getId() == null ||
                !slot.getBabysitter().getId().equals(request.getBabysitterId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The requested slot does not belong to the specified babysitter");
        }

        // Post-lock check: ensure slot was not already booked by a competing transaction
        if (Boolean.TRUE.equals(slot.getIsBooked())) {
            throw new ConflictException("The requested availability slot is already booked or no longer available");
        }

        // Validate slot has not started in the past
        if (slot.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot book a slot that has already started or is in the past");
        }

        // Calculate total amount based on duration (in hours) and babysitter hourly rate
        long durationMinutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
        double hours = durationMinutes / 60.0;
        Double hourlyRate = slot.getBabysitter().getHourlyRate();
        double totalAmount = Math.round(hours * hourlyRate * 100.0) / 100.0;

        // Reserve the slot atomically
        slot.setIsBooked(true);
        availabilitySlotRepository.save(slot);

        // Create booking in PENDING status
        Booking booking = Booking.builder()
                .parent(parent)
                .babysitter(slot.getBabysitter())
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Persist audit trail entry
        bookingAuditLogService.logCreation(
                savedBooking,
                parent,
                "Booking created with status PENDING"
        );

        log.info("Booking created with ID {} for parent '{}' and babysitter ID {}",
                savedBooking.getId(), parentEmail, slot.getBabysitter().getId());

        return BookingResponse.fromEntity(savedBooking);
    }

    /**
     * Confirms a PENDING booking. Only the assigned babysitter can confirm.
     */
    @Transactional
    public BookingResponse confirmBooking(Long bookingId, String babysitterEmail) {
        Booking booking = findBookingWithDetails(bookingId);
        User babysitterUser = findUser(babysitterEmail);

        validateBabysitterOwnership(booking, babysitterEmail);

        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only PENDING bookings can be confirmed. Current status: " + booking.getStatus());
        }

        booking.setStatus(Booking.Status.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        bookingAuditLogService.logStatusChange(
                saved,
                Booking.Status.PENDING,
                Booking.Status.CONFIRMED,
                babysitterUser,
                "Booking confirmed by babysitter"
        );

        return BookingResponse.fromEntity(saved);
    }

    /**
     * Declines a PENDING booking and releases the slot. Only the assigned babysitter can decline.
     */
    @Transactional
    public BookingResponse declineBooking(Long bookingId, String babysitterEmail) {
        Booking booking = findBookingWithDetails(bookingId);
        User babysitterUser = findUser(babysitterEmail);

        validateBabysitterOwnership(booking, babysitterEmail);

        if (booking.getStatus() != Booking.Status.PENDING) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only PENDING bookings can be declined. Current status: " + booking.getStatus());
        }

        booking.setStatus(Booking.Status.DECLINED);

        // Release the availability slot so another parent may book it
        AvailabilitySlot slot = booking.getAvailabilitySlot();
        slot.setIsBooked(false);
        availabilitySlotRepository.save(slot);

        Booking saved = bookingRepository.save(booking);

        bookingAuditLogService.logStatusChange(
                saved,
                Booking.Status.PENDING,
                Booking.Status.DECLINED,
                babysitterUser,
                "Booking declined by babysitter; slot released"
        );

        return BookingResponse.fromEntity(saved);
    }

    /**
     * Marks a CONFIRMED booking as COMPLETED. Only the assigned babysitter can complete.
     */
    @Transactional
    public BookingResponse completeBooking(Long bookingId, String babysitterEmail) {
        Booking booking = findBookingWithDetails(bookingId);
        User babysitterUser = findUser(babysitterEmail);

        validateBabysitterOwnership(booking, babysitterEmail);

        if (booking.getStatus() != Booking.Status.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Only CONFIRMED bookings can be completed. Current status: " + booking.getStatus());
        }

        booking.setStatus(Booking.Status.COMPLETED);
        Booking saved = bookingRepository.save(booking);

        bookingAuditLogService.logStatusChange(
                saved,
                Booking.Status.CONFIRMED,
                Booking.Status.COMPLETED,
                babysitterUser,
                "Booking marked as completed by babysitter"
        );

        return BookingResponse.fromEntity(saved);
    }

    /**
     * Cancels a PENDING or CONFIRMED booking and releases the slot. Only the booking's parent can cancel.
     */
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String parentEmail) {
        Booking booking = findBookingWithDetails(bookingId);
        User parentUser = findUser(parentEmail);

        if (!booking.getParent().getEmail().equals(parentEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the parent who created the booking can cancel it");
        }

        if (booking.getStatus() != Booking.Status.PENDING && booking.getStatus() != Booking.Status.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel booking with status: " + booking.getStatus() + ". Only PENDING or CONFIRMED bookings can be cancelled.");
        }

        Booking.Status oldStatus = booking.getStatus();
        booking.setStatus(Booking.Status.CANCELLED);

        // Release slot back into circulation
        AvailabilitySlot slot = booking.getAvailabilitySlot();
        slot.setIsBooked(false);
        availabilitySlotRepository.save(slot);

        Booking saved = bookingRepository.save(booking);

        bookingAuditLogService.logStatusChange(
                saved,
                oldStatus,
                Booking.Status.CANCELLED,
                parentUser,
                "Booking cancelled by parent; slot released"
        );

        return BookingResponse.fromEntity(saved);
    }

    /**
     * Retrieves the booking history for the authenticated user based on role.
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(String userEmail) {
        User user = findUser(userEmail);

        List<Booking> bookings;
        if (user.getRole() == User.Role.PARENT) {
            bookings = bookingRepository.findByParentEmailWithDetails(userEmail);
        } else if (user.getRole() == User.Role.BABYSITTER) {
            bookings = bookingRepository.findByBabysitterEmailWithDetails(userEmail);
        } else {
            // ADMIN
            bookings = bookingRepository.findAllWithDetails();
        }

        return bookings.stream().map(BookingResponse::fromEntity).toList();
    }

    /**
     * Retrieves a single booking if the user is authorized (parent, babysitter, or admin).
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, String userEmail) {
        Booking booking = findBookingWithDetails(bookingId);
        User user = findUser(userEmail);

        boolean isParent = booking.getParent().getEmail().equals(userEmail);
        boolean isBabysitter = booking.getBabysitter().getUser().getEmail().equals(userEmail);
        boolean isAdmin = user.getRole() == User.Role.ADMIN;

        if (!isParent && !isBabysitter && !isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to view this booking");
        }

        return BookingResponse.fromEntity(booking);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private Booking findBookingWithDetails(Long bookingId) {
        return bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    private void validateBabysitterOwnership(Booking booking, String babysitterEmail) {
        if (!booking.getBabysitter().getUser().getEmail().equals(babysitterEmail)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the assigned babysitter can perform this action");
        }
    }
}
