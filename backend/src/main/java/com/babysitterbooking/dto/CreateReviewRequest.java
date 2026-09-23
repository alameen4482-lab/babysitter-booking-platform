package com.babysitterbooking.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request body for submitting a review. */
@Getter
@NoArgsConstructor
public class CreateReviewRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    /** Optional comment — may be null or blank. */
    private String comment;
}
