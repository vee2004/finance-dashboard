package com.finance.dashboard.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified error body returned for all error responses.
 * Having a consistent shape lets API consumers parse errors predictably.
 */
@Data
@Builder
@Schema(description = "Standard API error response")
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;

    /**
     * Optional: populated only for validation errors.
     * Key = field name, Value = validation message.
     */
    private Map<String, String> fieldErrors;
}
