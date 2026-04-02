package com.finance.dashboard.service;

import com.finance.dashboard.dto.UserCreateRequest;
import com.finance.dashboard.dto.UserResponse;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.entity.UserStatus;
import com.finance.dashboard.exception.DuplicateResourceException;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Create ────────────────────────────────────────────────────────────────

    public UserResponse createUser(UserCreateRequest request) {
        // Guard against duplicate username or email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        return toResponse(userRepository.save(user));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    // ── Update status ─────────────────────────────────────────────────────────

    public UserResponse toggleStatus(Long id) {
        User user = findUserOrThrow(id);
        user.setStatus(user.getStatus() == UserStatus.ACTIVE
                ? UserStatus.INACTIVE
                : UserStatus.ACTIVE);
        return toResponse(userRepository.save(user));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
