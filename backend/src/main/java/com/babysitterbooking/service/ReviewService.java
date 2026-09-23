package com.babysitterbooking.service;

import com.babysitterbooking.dto.CreateReviewRequest;
import com.babysitterbooking.dto.ReviewResponse;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.exception.ResourceNotFoundException;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.Review;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.BabysitterRepository;
import com.babysitterbooking.repository.BookingRepository;
import com.babysitterbooking.repository.ReviewRepository;
import com.babysitterbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final BabysitterRepository babysitterRepository;
    private final UserRepository userRepository;

    /**
     * Creates a review for a COMPLETED booking.
     *
     * <p>Business rules enforced:
     * <ol>
     *   <li>Booking must exist.</li>
     *   <li>The authenticated parent must own this booking.</li>
     *   <li>Booking status must be COMPLETED.</li>
     *   <li>No prior review may exist for this booking (one review per booking).</li>
     *   <li>After saving, the babysitter's averageRating is recalculated.</li>
     * </ol>
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, String parentEmail) {

        Booking booking = bookingRepository.findByIdWithDetails(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + request.getBookingId()));

        // Verify the authenticated user is the parent of this booking
        if (!booking.getParent().getEmail().equals(parentEmail)) {
            throw new AccessDeniedException("You can only review your own bookings");
        }

        // Booking must be COMPLETED
        if (booking.getStatus() != Booking.Status.COMPLETED) {
            throw new ConflictException(
                    "You can only review a booking that has been completed. " +
                    "Current status: " + booking.getStatus());
        }

        // One review per booking
        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new ConflictException("A review for this booking already exists");
        }

        User parent = booking.getParent();
        Babysitter babysitter = booking.getBabysitter();

        Review review = Review.builder()
                .booking(booking)
                .parent(parent)
                .babysitter(babysitter)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // Recalculate and persist babysitter's average rating
        Double avg = reviewRepository.findAverageRatingByBabysitterId(babysitter.getId());
        babysitter.setAverageRating(avg != null ? avg : 0.0);
        babysitterRepository.save(babysitter);

        return ReviewResponse.from(review);
    }

    /** Returns all reviews for a specific babysitter. */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForBabysitter(Long babysitterId) {
        // Verify babysitter exists
        babysitterRepository.findById(babysitterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Babysitter not found with id: " + babysitterId));

        return reviewRepository.findByBabysitterId(babysitterId)
                .stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /** Returns the average rating for a specific babysitter. Returns 0.0 if no reviews exist. */
    @Transactional(readOnly = true)
    public Double getAverageRatingForBabysitter(Long babysitterId) {
        // Verify babysitter exists
        babysitterRepository.findById(babysitterId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Babysitter not found with id: " + babysitterId));

        Double avg = reviewRepository.findAverageRatingByBabysitterId(babysitterId);
        return avg != null ? avg : 0.0;
    }
}
