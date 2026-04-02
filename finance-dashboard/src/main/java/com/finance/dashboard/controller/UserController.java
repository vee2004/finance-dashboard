package com.finance.dashboard.controller;

import com.finance.dashboard.dto.UserResponse;
import com.finance.dashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin-only user management operations")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user list"),
            @ApiResponse(responseCode = "403", description = "Access denied – Admin role required")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user status (ACTIVE ↔ INACTIVE)",
               description = "Activates an inactive user or deactivates an active user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status toggled successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> toggleStatus(
            @Parameter(description = "User ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleStatus(id));
    }
}
