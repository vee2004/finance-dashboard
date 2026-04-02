package com.finance.dashboard.service;

import com.finance.dashboard.dto.DashboardSummaryResponse;
import com.finance.dashboard.dto.CategoryTotalResponse;
import com.finance.dashboard.dto.MonthlyTotalResponse;
import com.finance.dashboard.entity.RecordType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private FinancialRecordService financialRecordService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("getSummary: returns correct income, expense and net balance")
    void getSummary_returnsCorrectTotals() {
        when(recordRepository.sumByTypeAndDeletedFalse(RecordType.INCOME))
                .thenReturn(new BigDecimal("50000.00"));
        when(recordRepository.sumByTypeAndDeletedFalse(RecordType.EXPENSE))
                .thenReturn(new BigDecimal("30000.00"));
        when(recordRepository.countActive()).thenReturn(42L);

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getTotalIncome()).isEqualByComparingTo("50000.00");
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo("30000.00");
        assertThat(summary.getNetBalance()).isEqualByComparingTo("20000.00");
        assertThat(summary.getTotalRecords()).isEqualTo(42L);
    }

    @Test
    @DisplayName("getSummary: net balance is negative when expenses exceed income")
    void getSummary_expensesExceedIncome_negativeBalance() {
        when(recordRepository.sumByTypeAndDeletedFalse(RecordType.INCOME))
                .thenReturn(new BigDecimal("10000.00"));
        when(recordRepository.sumByTypeAndDeletedFalse(RecordType.EXPENSE))
                .thenReturn(new BigDecimal("15000.00"));
        when(recordRepository.countActive()).thenReturn(5L);

        DashboardSummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getNetBalance()).isNegative();
        assertThat(summary.getNetBalance()).isEqualByComparingTo("-5000.00");
    }

    @Test
    @DisplayName("getCategoryTotals: delegates to repository and returns all categories")
    void getCategoryTotals_returnsCategoryList() {
        List<CategoryTotalResponse> expected = List.of(
                new CategoryTotalResponse("Salary", new BigDecimal("45000.00")),
                new CategoryTotalResponse("Rent", new BigDecimal("12000.00"))
        );
        when(recordRepository.findCategoryTotals()).thenReturn(expected);

        List<CategoryTotalResponse> result = dashboardService.getCategoryTotals();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategory()).isEqualTo("Salary");
        assertThat(result.get(1).getTotal()).isEqualByComparingTo("12000.00");
    }

    @Test
    @DisplayName("getMonthlySummary: uses current year when year param is null")
    void getMonthlySummary_nullYear_usesCurrentYear() {
        List<MonthlyTotalResponse> monthly = List.of(
                new MonthlyTotalResponse(2024, 1, new BigDecimal("5000"), new BigDecimal("3000"))
        );
        when(recordRepository.findMonthlyTotals(anyInt())).thenReturn(monthly);

        List<MonthlyTotalResponse> result = dashboardService.getMonthlySummary(null);

        assertThat(result).hasSize(1);
        // Verify that the year passed to repo is the current year (not 0 or null)
        verify(recordRepository).findMonthlyTotals(java.time.LocalDate.now().getYear());
    }
}
