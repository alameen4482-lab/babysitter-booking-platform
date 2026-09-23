package com.babysitterbooking.controller;

import com.babysitterbooking.dto.ApiResponse;
import com.babysitterbooking.dto.BookingAuditLogResponse;
import com.babysitterbooking.dto.BookingResponse;
import com.babysitterbooking.exception.ResourceNotFoundException;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.BookingRepository;
import com.babysitterbooking.repository.UserRepository;
import com.babysitterbooking.service.BookingAuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative operations — ADMIN role required")
public class AdminController {

    private final BookingRepository bookingRepository;
    private final BookingAuditLogService bookingAuditLogService;
    private final UserRepository userRepository;

    @Operation(summary = "List all bookings in the system")
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> bookings = bookingRepository.findAllWithDetails()
                .stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved", bookings));
    }

    @Operation(summary = "Get the audit log for a specific booking")
    @GetMapping("/audit-logs/{bookingId}")
    public ResponseEntity<ApiResponse<List<BookingAuditLogResponse>>> getAuditLogs(
            @PathVariable Long bookingId) {
        List<BookingAuditLogResponse> logs = bookingAuditLogService.getLogsForBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", logs));
    }

    @Operation(summary = "Deactivate a user account")
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User account deactivated", null));
    }

    @Operation(summary = "Activate a previously deactivated user account")
    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User account activated", null));
    }
}
