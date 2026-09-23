package com.babysitterbooking.repository;

import com.babysitterbooking.model.entity.BookingAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingAuditLogRepository extends JpaRepository<BookingAuditLog, Long> {

    @Query("SELECT l FROM BookingAuditLog l " +
           "JOIN FETCH l.changedBy u " +
           "WHERE l.booking.id = :bookingId " +
           "ORDER BY l.createdAt ASC")
    List<BookingAuditLog> findByBookingIdOrderByCreatedAtAsc(@Param("bookingId") Long bookingId);
}
