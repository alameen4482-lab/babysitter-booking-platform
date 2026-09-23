package com.babysitterbooking.service;

import com.babysitterbooking.dto.AvailabilitySlotResponse;
import com.babysitterbooking.dto.CreateAvailabilitySlotRequest;
import com.babysitterbooking.exception.ApiException;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.exception.ResourceNotFoundException;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BabysitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilitySlotService {

    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final BabysitterRepository babysitterRepository;

    /**
     * Creates a new availability slot for the authenticated babysitter.
     *
     * @param request   the slot creation payload (start and end times)
     * @param userEmail the email of the authenticated user
     * @return the created slot DTO
     */
    @Transactional
    public AvailabilitySlotResponse createSlot(CreateAvailabilitySlotRequest request, String userEmail) {
        Babysitter babysitter = babysitterRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Babysitter profile not found for user: " + userEmail));

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = request.getEndTime();

        if (startTime == null || endTime == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start time and end time are required");
        }

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Start time cannot be in the past");
        }

        if (!endTime.isAfter(startTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        // Prevent overlapping slots for the same babysitter
        if (availabilitySlotRepository.existsOverlappingSlot(babysitter.getId(), startTime, endTime)) {
            throw new ConflictException("An availability slot already exists that overlaps with the specified time range");
        }

        AvailabilitySlot slot = AvailabilitySlot.builder()
                .babysitter(babysitter)
                .startTime(startTime)
                .endTime(endTime)
                .isBooked(false)
                .build();

        AvailabilitySlot savedSlot = availabilitySlotRepository.save(slot);
        log.info("Created availability slot ID {} for babysitter ID {}", savedSlot.getId(), babysitter.getId());

        return AvailabilitySlotResponse.fromEntity(savedSlot);
    }

    /**
     * Searches for available (unbooked) slots matching optional criteria.
     *
     * @param babysitterId optional babysitter ID filter
     * @param startTime    optional lower bound for slot start time
     * @param endTime      optional upper bound for slot end time
     * @return list of matching available slots
     */
    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> searchSlots(Long babysitterId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        List<AvailabilitySlot> slots = availabilitySlotRepository.searchAvailableSlots(babysitterId, startTime, endTime);
        return slots.stream()
                .map(AvailabilitySlotResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves an availability slot by its primary ID.
     *
     * @param id slot ID
     * @return slot DTO
     */
    @Transactional(readOnly = true)
    public AvailabilitySlotResponse getSlotById(Long id) {
        AvailabilitySlot slot = availabilitySlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AvailabilitySlot", id));

        return AvailabilitySlotResponse.fromEntity(slot);
    }
}
