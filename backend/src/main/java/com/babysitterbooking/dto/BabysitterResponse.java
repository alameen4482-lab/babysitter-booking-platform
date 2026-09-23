package com.babysitterbooking.dto;

import com.babysitterbooking.model.entity.Babysitter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Safe, serializable view of a Babysitter (no circular references). */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BabysitterResponse {

    private Long id;
    private String name;         // from User
    private String email;        // from User
    private String experience;
    private String skills;
    private Double hourlyRate;
    private Boolean available;
    private String bio;
    private Boolean isVerified;
    private Double averageRating;

    /** Factory method to convert a Babysitter entity to this DTO. */
    public static BabysitterResponse from(Babysitter b) {
        return BabysitterResponse.builder()
                .id(b.getId())
                .name(b.getUser() != null ? b.getUser().getName() : null)
                .email(b.getUser() != null ? b.getUser().getEmail() : null)
                .experience(b.getExperience())
                .skills(b.getSkills())
                .hourlyRate(b.getHourlyRate())
                .available(b.getAvailable())
                .bio(b.getBio())
                .isVerified(b.getIsVerified())
                .averageRating(b.getAverageRating())
                .build();
    }
}
