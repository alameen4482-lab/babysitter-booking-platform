package com.babysitterbooking.service;

import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.model.entity.User;
import com.babysitterbooking.repository.BabysitterRepository;
import com.babysitterbooking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BabysitterService {

    private final BabysitterRepository babysitterRepository;
    private final UserRepository userRepository;

    public BabysitterService(
            BabysitterRepository babysitterRepository,
            UserRepository userRepository) {

        this.babysitterRepository = babysitterRepository;
        this.userRepository = userRepository;
    }

    public Babysitter createBabysitter(Babysitter babysitter, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        babysitter.setUser(user);

        return babysitterRepository.save(babysitter);
    }

    public List<Babysitter> getAllBabysitters() {
        return babysitterRepository.findAll();
    }

    public Babysitter getBabysitterById(Long id) {
        return babysitterRepository.findById(id).orElse(null);
    }
}