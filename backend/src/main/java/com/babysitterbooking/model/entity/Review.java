package com.babysitterbooking.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Represents a review left by a parent for a babysitter after a COMPLETED booking.
 *
 * <p>Constraints:
 * <ul>
 *   <li>One review per booking (unique FK on booking_id).</li>
 *   <li>Only the parent who created the booking may post the review.</li>
 *   <li>The booking must be in COMPLETED status.</li>
 *   <li>Rating must be between 1 and 5 inclusive.</li>
 * </ul>
 */
@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    /** The booking this review is for. One booking → at most one review. */
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    /** The parent who wrote the review. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    /** The babysitter being reviewed. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "babysitter_id", nullable = false)
    private Babysitter babysitter;

    /** Rating between 1 (worst) and 5 (best). */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    /** Optional free-text comment. */
    @Column(columnDefinition = "TEXT")
    private String comment;
}
