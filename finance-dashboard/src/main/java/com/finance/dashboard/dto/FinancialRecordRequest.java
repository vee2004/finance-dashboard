package com.finance.dashboard.dto;

import com.finance.dashboard.entity.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Request to create or update a financial record")
public class FinancialRecordRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 2 decimal places")
    @Schema(example = "1500.00")
    private BigDecimal amount;

    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    @Schema(example = "INCOME", allowableValues = {"INCOME", "EXPENSE"})
    private RecordType type;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be at most 100 characters")
    @Schema(example = "Salary")
    private String category;

    @NotNull(message = "Date is required")
    @Schema(example = "2024-03-15")
    private LocalDate date;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    @Schema(example = "Monthly salary payment")
    private String notes;
}
