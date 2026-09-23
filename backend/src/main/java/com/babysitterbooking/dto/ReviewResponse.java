package com.babysitterbooking.dto;

import com.babysitterbooking.model.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Safe, serializable view of a Review. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long babysitterId;
    private String babysitterName;
    private Long parentId;
    private String parentName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    /** Factory method to convert a Review entity to this DTO. */
    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking() != null ? r.getBooking().getId() : null)
                .babysitterId(r.getBabysitter() != null ? r.getBabysitter().getId() : null)
                .babysitterName(r.getBabysitter() != null && r.getBabysitter().getUser() != null
                        ? r.getBabysitter().getUser().getName() : null)
                .parentId(r.getParent() != null ? r.getParent().getId() : null)
                .parentName(r.getParent() != null ? r.getParent().getName() : null)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
