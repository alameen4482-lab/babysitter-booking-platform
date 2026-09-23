package com.babysitterbooking.repository;

import com.babysitterbooking.model.entity.AvailabilitySlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    /**
     * Finds all unbooked slots for a given babysitter ordered by start time.
     */
    List<AvailabilitySlot> findByBabysitterIdAndIsBookedFalseOrderByStartTimeAsc(Long babysitterId);

    /**
     * Search available (unbooked) slots with optional filter criteria:
     * - babysitterId
     * - startTime (slots starting on or after this time)
     * - endTime (slots ending on or before this time)
     */
    @Query("SELECT s FROM AvailabilitySlot s JOIN FETCH s.babysitter b JOIN FETCH b.user u " +
           "WHERE s.isBooked = false " +
           "AND (:babysitterId IS NULL OR s.babysitter.id = :babysitterId) " +
           "AND (:startTime IS NULL OR s.startTime >= :startTime) " +
           "AND (:endTime IS NULL OR s.endTime <= :endTime) " +
           "ORDER BY s.startTime ASC")
    List<AvailabilitySlot> searchAvailableSlots(
            @Param("babysitterId") Long babysitterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Checks if an overlapping slot exists for the given babysitter.
     * Two intervals [S1, E1] and [S2, E2] overlap when S1 < E2 AND E1 > S2.
     */
    @Query("SELECT COUNT(s) > 0 FROM AvailabilitySlot s " +
           "WHERE s.babysitter.id = :babysitterId " +
           "AND s.startTime < :endTime AND s.endTime > :startTime")
    boolean existsOverlappingSlot(
            @Param("babysitterId") Long babysitterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Prepared for pessimistic locking in booking transaction flows.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AvailabilitySlot s WHERE s.id = :id")
    Optional<AvailabilitySlot> findByIdWithPessimisticLock(@Param("id") Long id);
}
