package com.babysitterbooking.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * AvailabilitySlot entity representing a time window when a babysitter is available.
 *
 * <p>Crucial for the concurrency handling specialization:
 * uses JPA {@link Version} for optimistic locking to detect and prevent double-booking.
 */
@Entity
@Table(
    name = "availability_slots",
    indexes = {
        @Index(name = "idx_slot_babysitter_booked", columnList = "babysitter_id, is_booked"),
        @Index(name = "idx_slot_times", columnList = "start_time, end_time")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilitySlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "babysitter_id", nullable = false)
    private Babysitter babysitter;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;

    @Version
    @Column(name = "version")
    private Integer version;
}
