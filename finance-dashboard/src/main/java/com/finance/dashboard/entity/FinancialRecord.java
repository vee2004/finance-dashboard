package com.finance.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single financial transaction (income or expense).
 *
 * Soft delete: we never physically remove records so historical analytics remain accurate.
 * The `deleted` flag filters out logically deleted rows in all queries.
 */
@Entity
@Table(name = "financial_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Use BigDecimal for monetary amounts — never use double/float for money.
     * Precision 15, scale 2 covers up to 999 billion with two decimal places.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 500)
    private String notes;

    // Who created this record – many records belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // Soft delete – set to true instead of physically deleting
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
