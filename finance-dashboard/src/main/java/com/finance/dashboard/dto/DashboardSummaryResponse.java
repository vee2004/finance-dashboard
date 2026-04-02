package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "High-level dashboard financial summary")
public class DashboardSummaryResponse {

    @Schema(example = "50000.00")
    private BigDecimal totalIncome;

    @Schema(example = "30000.00")
    private BigDecimal totalExpenses;

    @Schema(example = "20000.00", description = "totalIncome - totalExpenses")
    private BigDecimal netBalance;

    @Schema(example = "42")
    private long totalRecords;
}
