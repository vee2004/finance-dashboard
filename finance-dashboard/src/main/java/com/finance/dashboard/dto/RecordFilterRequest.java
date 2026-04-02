package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Query parameters for filtering financial records")
public class RecordFilterRequest {

    @Schema(example = "2024-01-01")
    private LocalDate startDate;

    @Schema(example = "2024-12-31")
    private LocalDate endDate;

    @Schema(example = "Salary")
    private String category;

    @Schema(example = "INCOME", allowableValues = {"INCOME", "EXPENSE"})
    private String type;
}
