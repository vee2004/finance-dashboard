package com.finance.dashboard.controller;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD operations on financial records with filters and pagination")
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    // ── POST /api/records ─────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Create a financial record",
               description = "Available to ADMIN and ANALYST roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Record created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "403", description = "Viewer cannot create records")
    })
    public ResponseEntity<FinancialRecordResponse> createRecord(
            @Valid @RequestBody FinancialRecordRequest request) {
        return ResponseEntity.status(201).body(recordService.createRecord(request));
    }

    // ── GET /api/records ──────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "List records with optional filters and pagination",
               description = "All roles can read. Filter by date range, category, or type.")
    public ResponseEntity<PagedResponse<FinancialRecordResponse>> getRecords(
            @Parameter(description = "Start date (inclusive), format: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date (inclusive), format: yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Category name filter (case-insensitive)")
            @RequestParam(required = false) String category,

            @Parameter(description = "Record type: INCOME or EXPENSE")
            @RequestParam(required = false) String type,

            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {

        RecordFilterRequest filter = new RecordFilterRequest();
        filter.setStartDate(startDate);
        filter.setEndDate(endDate);
        filter.setCategory(category);
        filter.setType(type);

        return ResponseEntity.ok(recordService.getRecords(filter, page, size));
    }

    // ── GET /api/records/{id} ─────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "Get a single financial record by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Record found"),
            @ApiResponse(responseCode = "404", description = "Record not found or deleted")
    })
    public ResponseEntity<FinancialRecordResponse> getRecord(
            @Parameter(description = "Record ID") @PathVariable Long id) {
        return ResponseEntity.ok(recordService.getRecordById(id));
    }

    // ── PUT /api/records/{id} ─────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Update a financial record",
               description = "Available to ADMIN and ANALYST roles.")
    public ResponseEntity<FinancialRecordResponse> updateRecord(
            @Parameter(description = "Record ID") @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request) {
        return ResponseEntity.ok(recordService.updateRecord(id, request));
    }

    // ── DELETE /api/records/{id} ──────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a financial record (Admin only)",
               description = "Marks the record as deleted without removing it from the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Record deleted"),
            @ApiResponse(responseCode = "403", description = "Only Admin can delete"),
            @ApiResponse(responseCode = "404", description = "Record not found")
    })
    public ResponseEntity<Void> deleteRecord(
            @Parameter(description = "Record ID") @PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
