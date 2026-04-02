package com.finance.dashboard.service;

import com.finance.dashboard.dto.AuthResponse;
import com.finance.dashboard.dto.LoginRequest;
import com.finance.dashboard.dto.UserCreateRequest;
import com.finance.dashboard.dto.UserResponse;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * Authenticates the user via Spring Security's AuthenticationManager,
     * then issues a JWT. We intentionally delegate authentication to Spring
     * Security rather than manually checking passwords — this way we get
     * account-locked / disabled checks for free.
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        UserResponse userResponse = userService.toResponse(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }

    /**
     * Admin-triggered registration. Creates a user and immediately returns a token
     * so the new user can start operating without a separate login step.
     */
    public AuthResponse register(UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        User user = userRepository.findByUsername(created.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(created)
                .build();
    }
}
