package com.finance.dashboard.controller;

import com.finance.dashboard.dto.AuthResponse;
import com.finance.dashboard.dto.LoginRequest;
import com.finance.dashboard.dto.UserCreateRequest;
import com.finance.dashboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and user registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password. Returns a JWT token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register new user (Admin only)",
               description = "Create a new user account. Only ADMIN role can perform this.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.status(201).body(authService.register(request));
    }
}
