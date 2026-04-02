package com.finance.dashboard.dto;

import com.finance.dashboard.entity.Role;
import com.finance.dashboard.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "User information returned by the API")
public class UserResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "jane.doe")
    private String username;

    @Schema(example = "jane@example.com")
    private String email;

    @Schema(example = "ANALYST")
    private Role role;

    @Schema(example = "ACTIVE")
    private UserStatus status;

    private LocalDateTime createdAt;
}
