package com.babysitterbooking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotNull(message = "Babysitter ID is required")
    private Long babysitterId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    private String notes;
}
