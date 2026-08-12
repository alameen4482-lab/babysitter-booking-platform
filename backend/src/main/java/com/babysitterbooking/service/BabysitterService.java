package com.babysitterbooking.service;

import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.repository.BabysitterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BabysitterService {

    private final BabysitterRepository babysitterRepository;

    public BabysitterService(BabysitterRepository babysitterRepository) {
        this.babysitterRepository = babysitterRepository;
    }

    public Babysitter createBabysitter(Babysitter babysitter) {
        return babysitterRepository.save(babysitter);
    }

    public List<Babysitter> getAllBabysitters() {
        return babysitterRepository.findAll();
    }

    public Babysitter getBabysitterById(Long id) {
        return babysitterRepository.findById(id).orElse(null);
    }
}