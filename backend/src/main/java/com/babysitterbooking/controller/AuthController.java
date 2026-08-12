package com.babysitterbooking.controller;

import com.babysitterbooking.dto.LoginRequest;
import com.babysitterbooking.dto.RegisterRequest;
import com.babysitterbooking.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String token = authService.register(request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Registration successful",
                        "token", token
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(request);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "token", token
                )
        );
    }
}