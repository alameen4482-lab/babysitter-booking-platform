package com.babysitterbooking.controller;

import com.babysitterbooking.dto.CreateBookingRequest;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.BookingAuditLog;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BabysitterRepository;
import com.babysitterbooking.repository.BookingAuditLogRepository;
import com.babysitterbooking.repository.BookingRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

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
    private BookingRepository bookingRepository;

    @Autowired
    private BookingAuditLogRepository bookingAuditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User parent1;
    private User parent2;
    private User sitterUser1;
    private User sitterUser2;
    private Babysitter babysitter1;
    private Babysitter babysitter2;
    private AvailabilitySlot slot1;
    private AvailabilitySlot slot2;

    @BeforeEach
    void setUp() {
        bookingAuditLogRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilitySlotRepository.deleteAll();
        babysitterRepository.deleteAll();
        userRepository.deleteAll();

        // Create Parent 1
        parent1 = User.builder()
                .name("Parent One")
                .email("parent1@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.PARENT)
                .build();
        userRepository.save(parent1);

        // Create Parent 2
        parent2 = User.builder()
                .name("Parent Two")
                .email("parent2@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.PARENT)
                .build();
        userRepository.save(parent2);

        // Create Babysitter 1
        sitterUser1 = User.builder()
                .name("Alice Sitter")
                .email("alice@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.BABYSITTER)
                .build();
        userRepository.save(sitterUser1);

        babysitter1 = Babysitter.builder()
                .user(sitterUser1)
                .hourlyRate(25.0)
                .experience("5 years")
                .skills("First Aid")
                .available(true)
                .build();
        babysitterRepository.save(babysitter1);

        // Create Babysitter 2
        sitterUser2 = User.builder()
                .name("Bob Sitter")
                .email("bob@test.com")
                .password(passwordEncoder.encode("secret"))
                .role(User.Role.BABYSITTER)
                .build();
        userRepository.save(sitterUser2);

        babysitter2 = Babysitter.builder()
                .user(sitterUser2)
                .hourlyRate(30.0)
                .experience("3 years")
                .skills("CPR")
                .available(true)
                .build();
        babysitterRepository.save(babysitter2);

        // Create Slots
        LocalDateTime start1 = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end1 = start1.plusHours(2); // 2h * $25 = $50.00
        slot1 = AvailabilitySlot.builder()
                .babysitter(babysitter1)
                .startTime(start1)
                .endTime(end1)
                .isBooked(false)
                .build();
        availabilitySlotRepository.save(slot1);

        LocalDateTime start2 = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end2 = start2.plusHours(3); // 3h * $30 = $90.00
        slot2 = AvailabilitySlot.builder()
                .babysitter(babysitter2)
                .startTime(start2)
                .endTime(end2)
                .isBooked(false)
                .build();
        availabilitySlotRepository.save(slot2);
    }

    @Test
    @DisplayName("POST /api/bookings - Parent creates booking successfully (201 Created)")
    @WithMockUser(username = "parent1@test.com", roles = {"PARENT"})
    void createBooking_AsParent_Returns201Created() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(babysitter1.getId())
                .slotId(slot1.getId())
                .notes("Need help with evening routine")
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(50.0))
                .andExpect(jsonPath("$.data.parentEmail").value("parent1@test.com"))
                .andExpect(jsonPath("$.data.babysitterName").value("Alice Sitter"));

        // Verify slot is marked booked
        AvailabilitySlot updatedSlot = availabilitySlotRepository.findById(slot1.getId()).orElseThrow();
        assertThat(updatedSlot.getIsBooked()).isTrue();

        // Verify audit log exists
        List<BookingAuditLog> logs = bookingAuditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getNewStatus()).isEqualTo(Booking.Status.PENDING);
    }

    @Test
    @DisplayName("POST /api/bookings - Babysitter cannot create booking (403 Forbidden)")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void createBooking_AsBabysitter_Returns403Forbidden() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(babysitter1.getId())
                .slotId(slot1.getId())
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/bookings - Unauthenticated user cannot create booking (401 Unauthorized)")
    void createBooking_Unauthenticated_Returns401Unauthorized() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(babysitter1.getId())
                .slotId(slot1.getId())
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/bookings - Cannot book an already booked slot (409 Conflict)")
    @WithMockUser(username = "parent1@test.com", roles = {"PARENT"})
    void createBooking_AlreadyBookedSlot_Returns409Conflict() throws Exception {
        slot1.setIsBooked(true);
        availabilitySlotRepository.save(slot1);

        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(babysitter1.getId())
                .slotId(slot1.getId())
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The requested availability slot is already booked or no longer available"));
    }

    @Test
    @DisplayName("POST /api/bookings - Cannot book a slot belonging to another babysitter (400 Bad Request)")
    @WithMockUser(username = "parent1@test.com", roles = {"PARENT"})
    void createBooking_SlotBabysitterMismatch_Returns400BadRequest() throws Exception {
        // slot2 belongs to babysitter2, but request specifies babysitter1
        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(babysitter1.getId())
                .slotId(slot2.getId())
                .build();

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("The requested slot does not belong to the specified babysitter"));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/confirm - Babysitter can confirm own booking")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void confirmBooking_AsAssignedBabysitter_Returns200OK() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        // Verify audit log
        List<BookingAuditLog> logs = bookingAuditLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getNewStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/confirm - Babysitter cannot confirm another babysitter's booking (403 Forbidden)")
    @WithMockUser(username = "bob@test.com", roles = {"BABYSITTER"})
    void confirmBooking_AsDifferentBabysitter_Returns403Forbidden() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1) // assigned to Alice, not Bob
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/confirm"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/decline - Babysitter declines booking and releases slot")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void declineBooking_AsAssignedBabysitter_DeclinesAndReleasesSlot() throws Exception {
        slot1.setIsBooked(true);
        availabilitySlotRepository.save(slot1);

        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/decline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DECLINED"));

        AvailabilitySlot refreshedSlot = availabilitySlotRepository.findById(slot1.getId()).orElseThrow();
        assertThat(refreshedSlot.getIsBooked()).isFalse();
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/complete - Babysitter completes CONFIRMED booking")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void completeBooking_AsAssignedBabysitter_CompletesBooking() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.CONFIRMED)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/complete - Fails if status is not CONFIRMED (400 Bad Request)")
    @WithMockUser(username = "alice@test.com", roles = {"BABYSITTER"})
    void completeBooking_WhenPending_Returns400BadRequest() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING) // not yet confirmed!
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/complete"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only CONFIRMED bookings can be completed. Current status: PENDING"));
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/cancel - Parent can cancel own booking and release slot")
    @WithMockUser(username = "parent1@test.com", roles = {"PARENT"})
    void cancelBooking_AsOwnerParent_CancelsAndReleasesSlot() throws Exception {
        slot1.setIsBooked(true);
        availabilitySlotRepository.save(slot1);

        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.CONFIRMED)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        AvailabilitySlot refreshedSlot = availabilitySlotRepository.findById(slot1.getId()).orElseThrow();
        assertThat(refreshedSlot.getIsBooked()).isFalse();
    }

    @Test
    @DisplayName("PATCH /api/bookings/{id}/cancel - Parent cannot cancel another parent's booking (403 Forbidden)")
    @WithMockUser(username = "parent2@test.com", roles = {"PARENT"})
    void cancelBooking_AsDifferentParent_Returns403Forbidden() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1) // belongs to Parent 1, not Parent 2
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(patch("/api/bookings/" + saved.getId() + "/cancel"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/bookings/my - Retrieves booking history for authenticated parent")
    @WithMockUser(username = "parent1@test.com", roles = {"PARENT"})
    void getMyBookings_AsParent_ReturnsBookings() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING)
                .totalAmount(50.0)
                .build();
        bookingRepository.save(booking);

        mockMvc.perform(get("/api/bookings/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].parentEmail").value("parent1@test.com"));
    }

    @Test
    @DisplayName("GET /api/bookings/{id} - Returns booking by ID for authorized participant")
    @WithMockUser(username = "parent1@test.com", roles = {"PARENT"})
    void getBookingById_AsAuthorizedParent_ReturnsBooking() throws Exception {
        Booking booking = Booking.builder()
                .parent(parent1)
                .babysitter(babysitter1)
                .availabilitySlot(slot1)
                .status(Booking.Status.PENDING)
                .totalAmount(50.0)
                .build();
        Booking saved = bookingRepository.save(booking);

        mockMvc.perform(get("/api/bookings/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()));
    }
}
