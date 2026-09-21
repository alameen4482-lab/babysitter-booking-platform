package com.babysitterbooking.controller;

import com.babysitterbooking.model.entity.Babysitter;
import com.babysitterbooking.service.BabysitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/babysitters")
public class BabysitterController {

    private final BabysitterService babysitterService;

    public BabysitterController(BabysitterService babysitterService) {
        this.babysitterService = babysitterService;
    }

    @PostMapping
    public ResponseEntity<Babysitter> createBabysitter(
            @RequestBody Babysitter babysitter,
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                babysitterService.createBabysitter(babysitter, email));
    }

    @GetMapping
    public ResponseEntity<List<Babysitter>> getAllBabysitters() {
        return ResponseEntity.ok(
                babysitterService.getAllBabysitters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Babysitter> getBabysitterById(
            @PathVariable Long id) {

        Babysitter babysitter = babysitterService.getBabysitterById(id);

        if (babysitter == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(babysitter);
    }
}