package com.babysitterbooking.service;

import com.babysitterbooking.dto.AvailabilitySlotResponse;
import com.babysitterbooking.dto.CreateAvailabilitySlotRequest;
import com.babysitterbooking.exception.ApiException;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.exception.ResourceNotFoundException;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BabysitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilitySlotServiceTest {

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private BabysitterRepository babysitterRepository;

    @InjectMocks
    private AvailabilitySlotService availabilitySlotService;

    private User user;
    private Babysitter babysitter;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("Jane Sitter")
                .email("jane@example.com")
                .password("encoded_pass")
                .role(User.Role.BABYSITTER)
                .build();

        babysitter = Babysitter.builder()
                .user(user)
                .experience("5 years")
                .skills("CPR, First Aid")
                .hourlyRate(25.0)
                .available(true)
                .build();

        futureStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        futureEnd = futureStart.plusHours(4);
    }

    @Test
    @DisplayName("createSlot: succeeds with valid future times and unbooked status")
    void createSlot_Success() {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart)
                .endTime(futureEnd)
                .build();

        when(babysitterRepository.findByUserEmail("jane@example.com")).thenReturn(Optional.of(babysitter));
        when(availabilitySlotRepository.existsOverlappingSlot(babysitter.getId(), futureStart, futureEnd)).thenReturn(false);

        AvailabilitySlot savedSlot = AvailabilitySlot.builder()
                .babysitter(babysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();

        when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(savedSlot);

        AvailabilitySlotResponse response = availabilitySlotService.createSlot(request, "jane@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getStartTime()).isEqualTo(futureStart);
        assertThat(response.getEndTime()).isEqualTo(futureEnd);
        assertThat(response.getIsBooked()).isFalse();
        assertThat(response.getHourlyRate()).isEqualTo(25.0);
        assertThat(response.getBabysitterName()).isEqualTo("Jane Sitter");
    }

    @Test
    @DisplayName("createSlot: throws ApiException when end time is before start time")
    void createSlot_EndTimeBeforeStartTime_ThrowsApiException() {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureEnd)
                .endTime(futureStart) // end is before start
                .build();

        when(babysitterRepository.findByUserEmail("jane@example.com")).thenReturn(Optional.of(babysitter));

        assertThatThrownBy(() -> availabilitySlotService.createSlot(request, "jane@example.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("End time must be after start time");

        verify(availabilitySlotRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSlot: throws ApiException when start time is in the past")
    void createSlot_StartTimeInPast_ThrowsApiException() {
        LocalDateTime pastStart = LocalDateTime.now().minusHours(2);
        LocalDateTime pastEnd = pastStart.plusHours(2);

        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(pastStart)
                .endTime(pastEnd)
                .build();

        when(babysitterRepository.findByUserEmail("jane@example.com")).thenReturn(Optional.of(babysitter));

        assertThatThrownBy(() -> availabilitySlotService.createSlot(request, "jane@example.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Start time cannot be in the past");

        verify(availabilitySlotRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSlot: throws ConflictException when slot overlaps with existing slot")
    void createSlot_OverlappingSlot_ThrowsConflictException() {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart)
                .endTime(futureEnd)
                .build();

        when(babysitterRepository.findByUserEmail("jane@example.com")).thenReturn(Optional.of(babysitter));
        when(availabilitySlotRepository.existsOverlappingSlot(babysitter.getId(), futureStart, futureEnd)).thenReturn(true);

        assertThatThrownBy(() -> availabilitySlotService.createSlot(request, "jane@example.com"))
                .isInstanceOf(ConflictException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("overlaps");

        verify(availabilitySlotRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSlot: throws ResourceNotFoundException when babysitter profile does not exist")
    void createSlot_BabysitterNotFound_ThrowsResourceNotFoundException() {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart)
                .endTime(futureEnd)
                .build();

        when(babysitterRepository.findByUserEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilitySlotService.createSlot(request, "unknown@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Babysitter profile not found");

        verify(availabilitySlotRepository, never()).save(any());
    }

    @Test
    @DisplayName("searchSlots: returns mapped available slots")
    void searchSlots_ReturnsAvailableSlots() {
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .babysitter(babysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();

        when(availabilitySlotRepository.searchAvailableSlots(null, futureStart, futureEnd))
                .thenReturn(List.of(slot));

        List<AvailabilitySlotResponse> results = availabilitySlotService.searchSlots(null, futureStart, futureEnd);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBabysitterName()).isEqualTo("Jane Sitter");
    }

    @Test
    @DisplayName("getSlotById: returns slot when found")
    void getSlotById_Success() {
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .babysitter(babysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();

        when(availabilitySlotRepository.findById(1L)).thenReturn(Optional.of(slot));

        AvailabilitySlotResponse result = availabilitySlotService.getSlotById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getStartTime()).isEqualTo(futureStart);
    }

    @Test
    @DisplayName("getSlotById: throws ResourceNotFoundException when not found")
    void getSlotById_NotFound_ThrowsResourceNotFoundException() {
        when(availabilitySlotRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilitySlotService.getSlotById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
