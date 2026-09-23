package com.babysitterbooking.dto;

import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.BookingAuditLog;
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
public class BookingAuditLogResponse {

    private Long id;
    private Long bookingId;
    private Booking.Status oldStatus;
    private Booking.Status newStatus;
    private Long changedById;
    private String changedByName;
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public static BookingAuditLogResponse fromEntity(BookingAuditLog log) {
        if (log == null) {
            return null;
        }

        Long changedById = null;
        String changedByName = null;
        if (log.getChangedBy() != null) {
            changedById = log.getChangedBy().getId();
            changedByName = log.getChangedBy().getName();
        }

        return BookingAuditLogResponse.builder()
                .id(log.getId())
                .bookingId(log.getBooking() != null ? log.getBooking().getId() : null)
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .changedById(changedById)
                .changedByName(changedByName)
                .remarks(log.getRemarks())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
