package com.babysitterbooking.controller;

import com.babysitterbooking.dto.ApiResponse;
import com.babysitterbooking.dto.AvailabilitySlotResponse;
import com.babysitterbooking.dto.CreateAvailabilitySlotRequest;
import com.babysitterbooking.service.AvailabilitySlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability-slots")
@RequiredArgsConstructor
@Tag(name = "Availability Slots", description = "Endpoints for managing and discovering babysitter availability slots")
public class AvailabilitySlotController {

    private final AvailabilitySlotService availabilitySlotService;

    /**
     * Creates a new availability slot.
     * Restricted strictly to users with the BABYSITTER role.
     */
    @PostMapping
    @PreAuthorize("hasRole('BABYSITTER')")
    @Operation(summary = "Create an availability slot", description = "Allows an authenticated babysitter to post an available time slot.")
    public ResponseEntity<ApiResponse<AvailabilitySlotResponse>> createSlot(
            @Valid @RequestBody CreateAvailabilitySlotRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        AvailabilitySlotResponse response = availabilitySlotService.createSlot(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Availability slot created successfully", response));
    }

    /**
     * Searches for available (unbooked) slots.
     * Accessible by any authenticated user (e.g. Parents searching for sitters).
     */
    @GetMapping
    @Operation(summary = "Search available slots", description = "Retrieve available slots with optional filters for babysitterId and time range.")
    public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> getAvailableSlots(
            @RequestParam(required = false) Long babysitterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        List<AvailabilitySlotResponse> slots = availabilitySlotService.searchSlots(babysitterId, startTime, endTime);

        return ResponseEntity.ok(ApiResponse.success("Available slots retrieved successfully", slots));
    }

    /**
     * Retrieves a specific availability slot by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get slot by ID", description = "Fetch details of a single availability slot.")
    public ResponseEntity<ApiResponse<AvailabilitySlotResponse>> getSlotById(@PathVariable Long id) {
        AvailabilitySlotResponse slot = availabilitySlotService.getSlotById(id);

        return ResponseEntity.ok(ApiResponse.success("Availability slot retrieved successfully", slot));
    }
}
