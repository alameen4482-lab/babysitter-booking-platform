package com.babysitterbooking.controller;

import com.babysitterbooking.dto.CreateAvailabilitySlotRequest;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BabysitterRepository;
import com.babysitterbooking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AvailabilitySlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BabysitterRepository babysitterRepository;

    @Autowired
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Babysitter savedBabysitter;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        availabilitySlotRepository.deleteAll();
        babysitterRepository.deleteAll();
        userRepository.deleteAll();

        User sitterUser = User.builder()
                .name("Alice Babysitter")
                .email("alice@test.com")
                .password(passwordEncoder.encode("secret123"))
                .role(User.Role.BABYSITTER)
                .build();
        userRepository.save(sitterUser);

        savedBabysitter = Babysitter.builder()
                .user(sitterUser)
                .experience("4 years")
                .skills("Infant Care, First Aid")
                .hourlyRate(22.0)
                .available(true)
                .build();
        babysitterRepository.save(savedBabysitter);

        User parentUser = User.builder()
                .name("Bob Parent")
                .email("bob@test.com")
                .password(passwordEncoder.encode("secret123"))
                .role(User.Role.PARENT)
                .build();
        userRepository.save(parentUser);

        futureStart = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        futureEnd = futureStart.plusHours(3);
    }

    @Test
    @DisplayName("POST /api/availability-slots - 201 Created for authenticated BABYSITTER")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void createSlot_AsBabysitter_Returns201Created() throws Exception {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart)
                .endTime(futureEnd)
                .build();

        mockMvc.perform(post("/api/availability-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Availability slot created successfully"))
                .andExpect(jsonPath("$.data.babysitterId").value(savedBabysitter.getId()))
                .andExpect(jsonPath("$.data.babysitterName").value("Alice Babysitter"))
                .andExpect(jsonPath("$.data.hourlyRate").value(22.0))
                .andExpect(jsonPath("$.data.isBooked").value(false));
    }

    @Test
    @DisplayName("POST /api/availability-slots - 403 Forbidden when authenticated as PARENT")
    @WithMockUser(username = "bob@test.com", roles = {"PARENT"})
    void createSlot_AsParent_Returns403Forbidden() throws Exception {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart)
                .endTime(futureEnd)
                .build();

        mockMvc.perform(post("/api/availability-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action."));
    }

    @Test
    @DisplayName("POST /api/availability-slots - 401 Unauthorized when request is unauthenticated")
    void createSlot_Unauthenticated_Returns401Unauthorized() throws Exception {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart)
                .endTime(futureEnd)
                .build();

        mockMvc.perform(post("/api/availability-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/availability-slots - 400 Bad Request when endTime <= startTime")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void createSlot_InvalidTimeRange_Returns400BadRequest() throws Exception {
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureEnd)
                .endTime(futureStart) // end time before start time
                .build();

        mockMvc.perform(post("/api/availability-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("End time must be after start time"));
    }

    @Test
    @DisplayName("POST /api/availability-slots - 409 Conflict when overlapping slot exists")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void createSlot_OverlappingSlot_Returns409Conflict() throws Exception {
        // Pre-create a slot
        AvailabilitySlot existingSlot = AvailabilitySlot.builder()
                .babysitter(savedBabysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();
        availabilitySlotRepository.save(existingSlot);

        // Attempt to create overlapping slot
        CreateAvailabilitySlotRequest request = CreateAvailabilitySlotRequest.builder()
                .startTime(futureStart.plusMinutes(30))
                .endTime(futureEnd.plusHours(1))
                .build();

        mockMvc.perform(post("/api/availability-slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An availability slot already exists that overlaps with the specified time range"));
    }

    @Test
    @DisplayName("GET /api/availability-slots - 200 OK returns available slots")
    @WithMockUser(username = "bob@test.com", roles = {"PARENT"})
    void getAvailableSlots_Returns200AndSlots() throws Exception {
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .babysitter(savedBabysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();
        availabilitySlotRepository.save(slot);

        mockMvc.perform(get("/api/availability-slots")
                        .param("babysitterId", savedBabysitter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].babysitterId").value(savedBabysitter.getId()));
    }

    @Test
    @DisplayName("GET /api/availability-slots/{id} - 200 OK returns slot by ID")
    @WithMockUser(username = "bob@test.com", roles = {"PARENT"})
    void getSlotById_Returns200AndSlot() throws Exception {
        AvailabilitySlot slot = AvailabilitySlot.builder()
                .babysitter(savedBabysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();
        AvailabilitySlot saved = availabilitySlotRepository.save(slot);

        mockMvc.perform(get("/api/availability-slots/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.isBooked").value(false));
    }

    @Test
    @DisplayName("GET /api/availability-slots/{id} - 404 Not Found for non-existent ID")
    @WithMockUser(username = "bob@test.com", roles = {"PARENT"})
    void getSlotById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/availability-slots/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
