package com.babysitterbooking.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Core transactional booking entity.
 * Records a parent's engagement with a babysitter for a specific availability slot.
 */
@Entity
@Table(
    name = "bookings",
    indexes = {
        @Index(name = "idx_booking_parent", columnList = "parent_id"),
        @Index(name = "idx_booking_babysitter", columnList = "babysitter_id"),
        @Index(name = "idx_booking_slot", columnList = "slot_id"),
        @Index(name = "idx_booking_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "babysitter_id", nullable = false)
    private Babysitter babysitter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private AvailabilitySlot availabilitySlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum Status {
        PENDING,
        CONFIRMED,
        DECLINED,
        COMPLETED,
        CANCELLED
    }
}
