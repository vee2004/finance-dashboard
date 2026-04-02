package com.finance.dashboard.controller;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Analytics and summary endpoints for the finance dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "Get financial summary",
               description = "Returns total income, total expenses, and net balance. Accessible to all roles.")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get category-wise totals",
               description = "Returns total amount per category, sorted by total descending. Requires ANALYST or ADMIN.")
    public ResponseEntity<List<CategoryTotalResponse>> getCategoryTotals() {
        return ResponseEntity.ok(dashboardService.getCategoryTotals());
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get monthly income/expense summary",
               description = "Returns monthly totals for the given year (defaults to current year). Requires ANALYST or ADMIN.")
    public ResponseEntity<List<MonthlyTotalResponse>> getMonthlySummary(
            @Parameter(description = "Year (defaults to current year)")
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(dashboardService.getMonthlySummary(year));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "Get recent transactions",
               description = "Returns the latest N transactions (default 10). Accessible to all roles.")
    public ResponseEntity<List<FinancialRecordResponse>> getRecentTransactions(
            @Parameter(description = "Number of recent transactions to return (max 50)")
            @RequestParam(defaultValue = "10") int limit) {
        int safeLimit = Math.min(limit, 50);  // cap at 50 to prevent abuse
        return ResponseEntity.ok(dashboardService.getRecentTransactions(safeLimit));
    }
}
