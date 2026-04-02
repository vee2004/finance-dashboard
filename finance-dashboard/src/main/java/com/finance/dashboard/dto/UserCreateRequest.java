package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.finance.dashboard.entity.Role;

@Data
@Schema(description = "Request payload to create a new user (Admin only)")
public class UserCreateRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3–50 characters")
    @Schema(example = "jane.doe")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "jane@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(example = "secret123")
    private String password;

    @NotNull(message = "Role is required")
    @Schema(example = "ANALYST", allowableValues = {"VIEWER", "ANALYST", "ADMIN"})
    private Role role;
}
