package com.finance.dashboard.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Single place for all exception-to-HTTP-response mappings.
 * Using @RestControllerAdvice instead of @ControllerAdvice avoids the need
 * for @ResponseBody on each handler — response is already JSON.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── 400 Bad Request ───────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("One or more fields are invalid")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperation(
            InvalidOperationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Operation", ex.getMessage(), request);
    }

    // ── 401 Unauthorized ──────────────────────────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password", request);
    }

    // ── 403 Forbidden ─────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to perform this action", request);
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    // ── 409 Conflict ──────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateResourceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    // ── 500 Fallback ──────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error,
                                                    String message, HttpServletRequest request) {
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(apiError);
    }
}
