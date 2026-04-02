package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "Monthly income and expense summary")
public class MonthlyTotalResponse {

    @Schema(example = "2024")
    private int year;

    @Schema(example = "3", description = "Month number (1 = January)")
    private int month;

    @Schema(example = "25000.00")
    private BigDecimal totalIncome;

    @Schema(example = "18000.00")
    private BigDecimal totalExpenses;
}
