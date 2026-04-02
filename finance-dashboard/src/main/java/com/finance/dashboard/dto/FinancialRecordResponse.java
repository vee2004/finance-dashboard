package com.finance.dashboard.dto;

import com.finance.dashboard.entity.RecordType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Financial record returned by the API")
public class FinancialRecordResponse {

    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;

    @Schema(example = "2024-03-15")
    private LocalDate date;

    private String notes;

    @Schema(description = "User who created this record")
    private String createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
