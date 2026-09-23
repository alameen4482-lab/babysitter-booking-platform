package com.babysitterbooking.dto;

import com.babysitterbooking.model.entity.AvailabilitySlot;
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
public class AvailabilitySlotResponse {

    private Long id;
    private Long babysitterId;
    private String babysitterName;
    private Double hourlyRate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private Boolean isBooked;
    private Integer version;

    public static AvailabilitySlotResponse fromEntity(AvailabilitySlot slot) {
        if (slot == null) {
            return null;
        }

        Long babysitterId = null;
        String babysitterName = null;
        Double hourlyRate = null;

        if (slot.getBabysitter() != null) {
            babysitterId = slot.getBabysitter().getId();
            hourlyRate = slot.getBabysitter().getHourlyRate();
            if (slot.getBabysitter().getUser() != null) {
                babysitterName = slot.getBabysitter().getUser().getName();
            }
        }

        return AvailabilitySlotResponse.builder()
                .id(slot.getId())
                .babysitterId(babysitterId)
                .babysitterName(babysitterName)
                .hourlyRate(hourlyRate)
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isBooked(slot.getIsBooked())
                .version(slot.getVersion())
                .build();
    }
}
