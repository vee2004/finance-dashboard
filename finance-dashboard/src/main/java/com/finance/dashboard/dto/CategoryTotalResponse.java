package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Schema(description = "Total amount grouped by category")
public class CategoryTotalResponse {

    @Schema(example = "Salary")
    private String category;

    @Schema(example = "45000.00")
    private BigDecimal total;
}
