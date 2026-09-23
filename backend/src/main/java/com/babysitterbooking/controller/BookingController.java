package com.babysitterbooking.controller;

import com.babysitterbooking.dto.ApiResponse;
import com.babysitterbooking.dto.BookingResponse;
import com.babysitterbooking.dto.CreateBookingRequest;
import com.babysitterbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Endpoints for booking requests, status lifecycle, and transaction management")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Creates a new booking request.
     * Restricted to authenticated users with role PARENT.
     */
    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Create booking", description = "Atomically reserves an availability slot using pessimistic locking and creates a booking in PENDING status.")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication
    ) {
        String parentEmail = authentication.getName();
        BookingResponse response = bookingService.createBooking(request, parentEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking request created successfully", response));
    }

    /**
     * Confirms a PENDING booking.
     * Restricted to authenticated babysitter assigned to the booking.
     */
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('BABYSITTER')")
    @Operation(summary = "Confirm booking", description = "Transitions booking status from PENDING to CONFIRMED.")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String babysitterEmail = authentication.getName();
        BookingResponse response = bookingService.confirmBooking(id, babysitterEmail);

        return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
    }

    /**
     * Declines a PENDING booking and releases the slot.
     * Restricted to authenticated babysitter assigned to the booking.
     */
    @PatchMapping("/{id}/decline")
    @PreAuthorize("hasRole('BABYSITTER')")
    @Operation(summary = "Decline booking", description = "Transitions booking status from PENDING to DECLINED and frees the slot.")
    public ResponseEntity<ApiResponse<BookingResponse>> declineBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String babysitterEmail = authentication.getName();
        BookingResponse response = bookingService.declineBooking(id, babysitterEmail);

        return ResponseEntity.ok(ApiResponse.success("Booking declined successfully", response));
    }

    /**
     * Marks a CONFIRMED booking as COMPLETED.
     * Restricted to authenticated babysitter assigned to the booking.
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('BABYSITTER')")
    @Operation(summary = "Complete booking", description = "Transitions booking status from CONFIRMED to COMPLETED.")
    public ResponseEntity<ApiResponse<BookingResponse>> completeBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String babysitterEmail = authentication.getName();
        BookingResponse response = bookingService.completeBooking(id, babysitterEmail);

        return ResponseEntity.ok(ApiResponse.success("Booking marked as completed", response));
    }

    /**
     * Cancels a PENDING or CONFIRMED booking and releases the slot.
     * Restricted to the parent who created the booking.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PARENT')")
    @Operation(summary = "Cancel booking", description = "Cancels a PENDING or CONFIRMED booking and releases the availability slot.")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String parentEmail = authentication.getName();
        BookingResponse response = bookingService.cancelBooking(id, parentEmail);

        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    /**
     * Retrieves the booking history for the logged-in user.
     */
    @GetMapping("/my")
    @Operation(summary = "Get user booking history", description = "Returns bookings for the authenticated parent or babysitter.")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(Authentication authentication) {
        String userEmail = authentication.getName();
        List<BookingResponse> bookings = bookingService.getMyBookings(userEmail);

        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    /**
     * Retrieves a specific booking by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID", description = "Returns booking details if authorized.")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userEmail = authentication.getName();
        BookingResponse response = bookingService.getBookingById(id, userEmail);

        return ResponseEntity.ok(ApiResponse.success("Booking retrieved successfully", response));
    }
}
