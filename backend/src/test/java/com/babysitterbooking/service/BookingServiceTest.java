package com.babysitterbooking.service;

import com.babysitterbooking.dto.BookingResponse;
import com.babysitterbooking.dto.CreateBookingRequest;
import com.babysitterbooking.exception.ApiException;
import com.babysitterbooking.exception.ConflictException;
import com.babysitterbooking.model.entity.AvailabilitySlot;
import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.AvailabilitySlotRepository;
import com.babysitterbooking.repository.BookingRepository;
import com.babysitterbooking.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingAuditLogService bookingAuditLogService;

    @InjectMocks
    private BookingService bookingService;

    private User parentUser;
    private User otherParentUser;
    private User sitterUser;
    private Babysitter babysitter;
    private AvailabilitySlot slot;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        parentUser = User.builder()
                .name("Parent One")
                .email("parent1@test.com")
                .password("pass")
                .role(User.Role.PARENT)
                .build();

        otherParentUser = User.builder()
                .name("Parent Two")
                .email("parent2@test.com")
                .password("pass")
                .role(User.Role.PARENT)
                .build();

        sitterUser = User.builder()
                .name("Jane Babysitter")
                .email("jane@test.com")
                .password("pass")
                .role(User.Role.BABYSITTER)
                .build();

        babysitter = Babysitter.builder()
                .user(sitterUser)
                .hourlyRate(20.0)
                .experience("5 years")
                .skills("CPR")
                .available(true)
                .build();

        futureStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        futureEnd = futureStart.plusHours(3); // 3 hours * $20 = $60.00

        slot = AvailabilitySlot.builder()
                .babysitter(babysitter)
                .startTime(futureStart)
                .endTime(futureEnd)
                .isBooked(false)
                .build();

        parentUser.setId(10L);
        otherParentUser.setId(11L);
        sitterUser.setId(20L);
        babysitter.setId(1L);
        slot.setId(100L);
    }

    @Test
    @DisplayName("createBooking: calculates total amount correctly, marks slot booked, sets PENDING status, and records audit")
    void createBooking_Success() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(1L)
                .slotId(100L)
                .notes("Please bring coloring books")
                .build();

        when(userRepository.findByEmail("parent1@test.com")).thenReturn(Optional.of(parentUser));
        when(availabilitySlotRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(slot));

        Booking savedBooking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .totalAmount(60.0)
                .notes("Please bring coloring books")
                .build();
        savedBooking.setId(1000L);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingResponse response = bookingService.createBooking(request, "parent1@test.com");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(Booking.Status.PENDING);
        assertThat(response.getTotalAmount()).isEqualTo(60.0);
        assertThat(slot.getIsBooked()).isTrue();

        verify(availabilitySlotRepository).save(slot);
        verify(bookingAuditLogService).logCreation(any(), eq(parentUser), any());
    }

    @Test
    @DisplayName("createBooking: throws ConflictException when slot is already booked")
    void createBooking_AlreadyBooked_ThrowsConflictException() {
        slot.setIsBooked(true);

        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(1L)
                .slotId(100L)
                .build();

        when(userRepository.findByEmail("parent1@test.com")).thenReturn(Optional.of(parentUser));
        when(availabilitySlotRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> bookingService.createBooking(request, "parent1@test.com"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already booked");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBooking: throws ApiException when slot belongs to a different babysitter")
    void createBooking_SlotMismatchedBabysitter_ThrowsApiException() {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .babysitterId(999L) // does not match slot.babysitter (id 1L)
                .slotId(100L)
                .build();

        when(userRepository.findByEmail("parent1@test.com")).thenReturn(Optional.of(parentUser));
        when(availabilitySlotRepository.findByIdWithPessimisticLock(100L)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> bookingService.createBooking(request, "parent1@test.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("does not belong to the specified babysitter");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmBooking: succeeds for assigned babysitter on PENDING booking")
    void confirmBooking_Success() {
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .totalAmount(60.0)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(sitterUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.confirmBooking(10L, "jane@test.com");

        assertThat(response.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
        verify(bookingAuditLogService).logStatusChange(any(), eq(Booking.Status.PENDING), eq(Booking.Status.CONFIRMED), eq(sitterUser), any());
    }

    @Test
    @DisplayName("confirmBooking: throws Forbidden when called by unassigned babysitter")
    void confirmBooking_WrongBabysitter_ThrowsForbidden() {
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("stranger@test.com")).thenReturn(Optional.of(sitterUser));

        assertThatThrownBy(() -> bookingService.confirmBooking(10L, "stranger@test.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("declineBooking: declines booking and releases availability slot")
    void declineBooking_Success() {
        slot.setIsBooked(true);
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(sitterUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.declineBooking(10L, "jane@test.com");

        assertThat(response.getStatus()).isEqualTo(Booking.Status.DECLINED);
        assertThat(slot.getIsBooked()).isFalse();
        verify(availabilitySlotRepository).save(slot);
    }

    @Test
    @DisplayName("completeBooking: succeeds on CONFIRMED booking")
    void completeBooking_Success() {
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.CONFIRMED)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("jane@test.com")).thenReturn(Optional.of(sitterUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.completeBooking(10L, "jane@test.com");

        assertThat(response.getStatus()).isEqualTo(Booking.Status.COMPLETED);
    }

    @Test
    @DisplayName("cancelBooking: parent cancels own booking and releases slot")
    void cancelBooking_Success() {
        slot.setIsBooked(true);
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("parent1@test.com")).thenReturn(Optional.of(parentUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.cancelBooking(10L, "parent1@test.com");

        assertThat(response.getStatus()).isEqualTo(Booking.Status.CANCELLED);
        assertThat(slot.getIsBooked()).isFalse();
        verify(availabilitySlotRepository).save(slot);
    }

    @Test
    @DisplayName("cancelBooking: throws Forbidden if another parent tries to cancel")
    void cancelBooking_WrongParent_ThrowsForbidden() {
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.PENDING)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("parent2@test.com")).thenReturn(Optional.of(otherParentUser));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, "parent2@test.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("cancelBooking: throws BadRequest if booking is already COMPLETED")
    void cancelBooking_AlreadyCompleted_ThrowsBadRequest() {
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.COMPLETED)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("parent1@test.com")).thenReturn(Optional.of(parentUser));

        assertThatThrownBy(() -> bookingService.cancelBooking(10L, "parent1@test.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("Cannot cancel booking with status: COMPLETED");
    }

    @Test
    @DisplayName("getBookingById: throws Forbidden for unauthorized user")
    void getBookingById_UnauthorizedUser_ThrowsForbidden() {
        Booking booking = Booking.builder()
                .parent(parentUser)
                .babysitter(babysitter)
                .availabilitySlot(slot)
                .status(Booking.Status.CONFIRMED)
                .build();

        when(bookingRepository.findByIdWithDetails(10L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("stranger@test.com")).thenReturn(Optional.of(otherParentUser));

        assertThatThrownBy(() -> bookingService.getBookingById(10L, "stranger@test.com"))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    }
}
