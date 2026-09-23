package com.babysitterbooking.dto;

import com.babysitterbooking.model.entity.Booking;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long parentId;
    private String parentName;
    private String parentEmail;

    private Long babysitterId;
    private String babysitterName;

    private Long slotId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private Booking.Status status;
    private Double totalAmount;
    private String notes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public static BookingResponse fromEntity(Booking booking) {
        if (booking == null) {
            return null;
        }

        Long parentId = null;
        String parentName = null;
        String parentEmail = null;
        if (booking.getParent() != null) {
            parentId = booking.getParent().getId();
            parentName = booking.getParent().getName();
            parentEmail = booking.getParent().getEmail();
        }

        Long babysitterId = null;
        String babysitterName = null;
        if (booking.getBabysitter() != null) {
            babysitterId = booking.getBabysitter().getId();
            if (booking.getBabysitter().getUser() != null) {
                babysitterName = booking.getBabysitter().getUser().getName();
            }
        }

        Long slotId = null;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (booking.getAvailabilitySlot() != null) {
            slotId = booking.getAvailabilitySlot().getId();
            startTime = booking.getAvailabilitySlot().getStartTime();
            endTime = booking.getAvailabilitySlot().getEndTime();
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .parentId(parentId)
                .parentName(parentName)
                .parentEmail(parentEmail)
                .babysitterId(babysitterId)
                .babysitterName(babysitterName)
                .slotId(slotId)
                .startTime(startTime)
                .endTime(endTime)
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .notes(booking.getNotes())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
