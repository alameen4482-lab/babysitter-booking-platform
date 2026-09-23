package com.babysitterbooking.controller;

import com.babysitterbooking.dto.ApiResponse;
import com.babysitterbooking.dto.BabysitterResponse;
import com.babysitterbooking.dto.CreateBabysitterRequest;
import com.babysitterbooking.service.BabysitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/babysitters")
@RequiredArgsConstructor
@Tag(name = "Babysitters", description = "Babysitter profile management")
public class BabysitterController {

    private final BabysitterService babysitterService;

    @Operation(summary = "Create a babysitter profile (BABYSITTER role only)")
    @PostMapping
    @PreAuthorize("hasRole('BABYSITTER')")
    public ResponseEntity<ApiResponse<BabysitterResponse>> createBabysitter(
            @Valid @RequestBody CreateBabysitterRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        BabysitterResponse response = babysitterService.createBabysitter(request, email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Babysitter profile created successfully", response));
    }

    @Operation(summary = "Get all babysitter profiles")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BabysitterResponse>>> getAllBabysitters() {
        return ResponseEntity.ok(
                ApiResponse.success("Babysitters retrieved successfully",
                        babysitterService.getAllBabysitters()));
    }

    @Operation(summary = "Get a babysitter by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BabysitterResponse>> getBabysitterById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Babysitter retrieved successfully",
                        babysitterService.getBabysitterById(id)));
    }

    @Operation(summary = "Search babysitters with optional time window and minimum rating filters")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BabysitterResponse>>> searchBabysitters(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Double minimumRating) {

        List<BabysitterResponse> results =
                babysitterService.searchBabysitters(startTime, endTime, minimumRating);
        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", results));
    }
}