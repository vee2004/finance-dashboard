package com.finance.dashboard.repository;

import com.finance.dashboard.dto.CategoryTotalResponse;
import com.finance.dashboard.dto.MonthlyTotalResponse;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // ── Basic lookup ──────────────────────────────────────────────────────────

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // ── Filtered paginated listing ────────────────────────────────────────────

    /**
     * Filters records by all optional parameters. Null values are ignored thanks
     * to the JPQL coalesce-style conditionals. This avoids building a Criteria API
     * query for a straightforward filter use-case.
     */
    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE r.deleted = false
              AND (:startDate IS NULL OR r.date >= :startDate)
              AND (:endDate   IS NULL OR r.date <= :endDate)
              AND (:category  IS NULL OR LOWER(r.category) = LOWER(:category))
              AND (:type      IS NULL OR r.type = :type)
            ORDER BY r.date DESC, r.createdAt DESC
            """)
    Page<FinancialRecord> findWithFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate")   LocalDate endDate,
            @Param("category")  String category,
            @Param("type")      RecordType type,
            Pageable pageable
    );

    // ── Dashboard aggregations ────────────────────────────────────────────────

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type AND r.deleted = false")
    BigDecimal sumByTypeAndDeletedFalse(@Param("type") RecordType type);

    @Query("SELECT COUNT(r) FROM FinancialRecord r WHERE r.deleted = false")
    long countActive();

    /**
     * Returns (category, total) pairs ordered by total descending.
     * Uses constructor expression — CategoryTotalResponse must have a matching constructor.
     */
    @Query("""
            SELECT new com.finance.dashboard.dto.CategoryTotalResponse(r.category, SUM(r.amount))
            FROM FinancialRecord r
            WHERE r.deleted = false
            GROUP BY r.category
            ORDER BY SUM(r.amount) DESC
            """)
    List<CategoryTotalResponse> findCategoryTotals();

    /**
     * Monthly totals for a given year using conditional SUM.
     * Returns one row per month that has at least one non-deleted record.
     */
    @Query("""
            SELECT new com.finance.dashboard.dto.MonthlyTotalResponse(
                YEAR(r.date), MONTH(r.date),
                COALESCE(SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END), 0)
            )
            FROM FinancialRecord r
            WHERE r.deleted = false AND YEAR(r.date) = :year
            GROUP BY YEAR(r.date), MONTH(r.date)
            ORDER BY MONTH(r.date)
            """)
    List<MonthlyTotalResponse> findMonthlyTotals(@Param("year") int year);

    /**
     * Recent N transactions ordered by date desc, used on the dashboard widget.
     */
    @Query("SELECT r FROM FinancialRecord r WHERE r.deleted = false ORDER BY r.date DESC, r.createdAt DESC")
    List<FinancialRecord> findRecentTransactions(Pageable pageable);
}
