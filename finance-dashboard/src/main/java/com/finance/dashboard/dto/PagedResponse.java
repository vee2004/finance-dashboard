package com.finance.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Generic pagination wrapper to avoid returning raw Page<T> from controllers.
 * Keeps the API contract explicit and controllable.
 */
@Data
@Builder
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {

    private List<T> content;

    @Schema(example = "0")
    private int page;

    @Schema(example = "10")
    private int size;

    @Schema(example = "42")
    private long totalElements;

    @Schema(example = "5")
    private int totalPages;

    @Schema(example = "false")
    private boolean last;
}
