package com.babysitterbooking.controller;

import com.babysitterbooking.dto.ApiResponse;
import com.babysitterbooking.dto.CreateReviewRequest;
import com.babysitterbooking.dto.ReviewResponse;
import com.babysitterbooking.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management for completed bookings")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Submit a review for a completed booking (PARENT only)")
    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        String parentEmail = authentication.getName();
        ReviewResponse response = reviewService.createReview(request, parentEmail);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", response));
    }

    @Operation(summary = "Get all reviews for a babysitter")
    @GetMapping("/babysitter/{babysitterId}")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsForBabysitter(
            @PathVariable Long babysitterId) {

        List<ReviewResponse> reviews = reviewService.getReviewsForBabysitter(babysitterId);
        return ResponseEntity.ok(
                ApiResponse.success("Reviews retrieved successfully", reviews));
    }

    @Operation(summary = "Get the average rating for a babysitter")
    @GetMapping("/babysitter/{babysitterId}/average")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @PathVariable Long babysitterId) {

        Double avg = reviewService.getAverageRatingForBabysitter(babysitterId);
        return ResponseEntity.ok(
                ApiResponse.success("Average rating retrieved successfully", avg));
    }
}
