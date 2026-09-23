package com.babysitterbooking.repository;

import com.babysitterbooking.model.entity.Babysitter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BabysitterRepository extends JpaRepository<Babysitter, Long> {

    Optional<Babysitter> findByUserId(Long userId);

    Optional<Babysitter> findByUserEmail(String email);
}