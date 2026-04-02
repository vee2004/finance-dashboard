package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "JWT authentication response")
public class AuthResponse {

    @Schema(description = "JWT Bearer token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(example = "Bearer")
    private String tokenType;

    private UserResponse user;
}
