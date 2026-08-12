package com.babysitterbooking.repository;

import com.babysitterbooking.model.entity.Babysitter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BabysitterRepository extends JpaRepository<Babysitter, Long> {
}