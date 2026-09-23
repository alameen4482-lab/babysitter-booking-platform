package com.babysitterbooking.service;

import com.babysitterbooking.dto.BookingAuditLogResponse;
import com.babysitterbooking.model.entity.Booking;
import com.babysitterbooking.model.entity.BookingAuditLog;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.BookingAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingAuditLogService {

    private final BookingAuditLogRepository bookingAuditLogRepository;

    /**
     * Persists an initial audit log entry when a booking is first created.
     * Uses Propagation.REQUIRED to share the same transaction with the initial booking insert,
     * maintaining foreign key referential integrity in relational databases.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public BookingAuditLog logCreation(Booking booking, User changedBy, String remarks) {
        return persistLog(booking, null, booking.getStatus(), changedBy, remarks);
    }

    /**
     * Persists an audit log entry in a dedicated, independent transaction.
     * Propagation.REQUIRES_NEW guarantees this log record commits independently
     * for state transitions on already-committed bookings.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingAuditLog logStatusChange(
            Booking booking,
            Booking.Status oldStatus,
            Booking.Status newStatus,
            User changedBy,
            String remarks
    ) {
        return persistLog(booking, oldStatus, newStatus, changedBy, remarks);
    }

    private BookingAuditLog persistLog(
            Booking booking,
            Booking.Status oldStatus,
            Booking.Status newStatus,
            User changedBy,
            String remarks
    ) {
        BookingAuditLog auditLog = BookingAuditLog.builder()
                .booking(booking)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .remarks(remarks)
                .build();

        BookingAuditLog savedLog = bookingAuditLogRepository.save(auditLog);
        log.info("Recorded audit log ID {} for booking ID {}: {} -> {}",
                savedLog.getId(), booking.getId(), oldStatus, newStatus);
        return savedLog;
    }

    /**
     * Retrieves the audit trail for a given booking.
     */
    @Transactional(readOnly = true)
    public List<BookingAuditLogResponse> getLogsForBooking(Long bookingId) {
        return bookingAuditLogRepository.findByBookingIdOrderByCreatedAtAsc(bookingId)
                .stream()
                .map(BookingAuditLogResponse::fromEntity)
                .toList();
    }
}
