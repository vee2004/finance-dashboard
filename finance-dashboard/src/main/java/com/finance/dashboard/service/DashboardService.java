package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.RecordType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final FinancialRecordRepository recordRepository;
    private final FinancialRecordService financialRecordService;

    // ── Summary ───────────────────────────────────────────────────────────────

    public DashboardSummaryResponse getSummary() {
        BigDecimal totalIncome   = recordRepository.sumByTypeAndDeletedFalse(RecordType.INCOME);
        BigDecimal totalExpenses = recordRepository.sumByTypeAndDeletedFalse(RecordType.EXPENSE);
        long totalRecords        = recordRepository.countActive();

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(totalIncome.subtract(totalExpenses))
                .totalRecords(totalRecords)
                .build();
    }

    // ── Category totals ───────────────────────────────────────────────────────

    public List<CategoryTotalResponse> getCategoryTotals() {
        return recordRepository.findCategoryTotals();
    }

    // ── Monthly summary ───────────────────────────────────────────────────────

    /**
     * Defaults to the current year if no year is provided.
     * Returns one entry per month that has records.
     */
    public List<MonthlyTotalResponse> getMonthlySummary(Integer year) {
        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        return recordRepository.findMonthlyTotals(targetYear);
    }

    // ── Recent transactions ───────────────────────────────────────────────────

    public List<FinancialRecordResponse> getRecentTransactions(int limit) {
        return recordRepository
                .findRecentTransactions(PageRequest.of(0, limit))
                .stream()
                .map(financialRecordService::toResponse)
                .collect(Collectors.toList());
    }
}
