package com.babysitterbooking.repository;

import com.babysitterbooking.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Check if a review already exists for this booking (one-review-per-booking). */
    boolean existsByBookingId(Long bookingId);

    /** All reviews for a given babysitter (for listing). */
    List<Review> findByBabysitterId(Long babysitterId);

    /** Average rating for a babysitter across all their reviews. Returns null when there are no reviews. */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.babysitter.id = :babysitterId")
    Double findAverageRatingByBabysitterId(@Param("babysitterId") Long babysitterId);
}
