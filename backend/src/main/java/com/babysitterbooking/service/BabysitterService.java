package com.babysitterbooking.service;

import com.babysitterbooking.dto.BabysitterResponse;
import com.babysitterbooking.dto.CreateBabysitterRequest;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.exception.ResourceNotFoundException;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BabysitterRepository;
import com.babysitterbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BabysitterService {

    private final BabysitterRepository babysitterRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;

    /**
     * Creates a babysitter profile for the authenticated user.
     * Throws {@link ConflictException} if the user already has a profile.
     */
    public BabysitterResponse createBabysitter(CreateBabysitterRequest request, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (babysitterRepository.findByUserEmail(email).isPresent()) {
            throw new ConflictException("A babysitter profile already exists for this account");
        }

        Babysitter babysitter = Babysitter.builder()
                .user(user)
                .experience(request.getExperience())
                .skills(request.getSkills())
                .hourlyRate(request.getHourlyRate())
                .bio(request.getBio())
                .build();

        return BabysitterResponse.from(babysitterRepository.save(babysitter));
    }

    /** Returns all babysitter profiles. */
    public List<BabysitterResponse> getAllBabysitters() {
        return babysitterRepository.findAll()
                .stream()
                .map(BabysitterResponse::from)
                .collect(Collectors.toList());
    }

    /** Returns a single babysitter by ID or throws {@link ResourceNotFoundException}. */
    public BabysitterResponse getBabysitterById(Long id) {
        Babysitter babysitter = babysitterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Babysitter not found with id: " + id));
        return BabysitterResponse.from(babysitter);
    }

    /**
     * Searches babysitters with optional filters.
     *
     * @param startTime     only include babysitters who have an available slot starting on or after this time
     * @param endTime       only include babysitters who have an available slot ending on or before this time
     * @param minimumRating only include babysitters whose averageRating >= this value
     */
    public List<BabysitterResponse> searchBabysitters(
            LocalDateTime startTime,
            LocalDateTime endTime,
            Double minimumRating) {

        List<Babysitter> all;

        if (startTime != null || endTime != null) {
            // Fetch distinct babysitters who have matching available slots
            all = availabilitySlotRepository
                    .searchAvailableSlots(null, startTime, endTime)
                    .stream()
                    .map(slot -> slot.getBabysitter())
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            all = babysitterRepository.findAll();
        }

        if (minimumRating != null) {
            all = all.stream()
                    .filter(b -> b.getAverageRating() != null && b.getAverageRating() >= minimumRating)
                    .collect(Collectors.toList());
        }

        return all.stream()
                .map(BabysitterResponse::from)
                .collect(Collectors.toList());
    }
}