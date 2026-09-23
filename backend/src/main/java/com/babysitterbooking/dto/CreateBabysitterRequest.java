package com.babysitterbooking.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Request body for creating a babysitter profile. */
@Getter
@NoArgsConstructor
public class CreateBabysitterRequest {

    @NotBlank(message = "Experience description is required")
    private String experience;

    @NotBlank(message = "Skills description is required")
    private String skills;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    private Double hourlyRate;

    /** Optional biography / about-me text. */
    private String bio;
}
