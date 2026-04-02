package com.finance.dashboard.config;

import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps a default Admin user on first startup if none exists.
 *
 * This is a convenience for local development. In production, remove or
 * replace this with a proper secret-management process (e.g., vault, env var).
 *
 * Default credentials:  username=admin  |  password=admin123
 * Change password immediately via the user update endpoint after first login.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            log.info("Default admin user already exists. Skipping seed.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@finance.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("✅ Default admin user created. username=admin, password=admin123 — CHANGE THIS IN PRODUCTION.");
    }
}
