package com.babysitterbooking.repository;

import com.babysitterbooking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.parent p " +
           "JOIN FETCH b.babysitter bs " +
           "JOIN FETCH bs.user bu " +
           "JOIN FETCH b.availabilitySlot s " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.parent p " +
           "JOIN FETCH b.babysitter bs " +
           "JOIN FETCH bs.user bu " +
           "JOIN FETCH b.availabilitySlot s " +
           "WHERE p.email = :email " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByParentEmailWithDetails(@Param("email") String email);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.parent p " +
           "JOIN FETCH b.babysitter bs " +
           "JOIN FETCH bs.user bu " +
           "JOIN FETCH b.availabilitySlot s " +
           "WHERE bu.email = :email " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByBabysitterEmailWithDetails(@Param("email") String email);

    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.parent p " +
           "JOIN FETCH b.babysitter bs " +
           "JOIN FETCH bs.user bu " +
           "JOIN FETCH b.availabilitySlot s " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetails();

    List<Booking> findByAvailabilitySlotId(Long slotId);
}
