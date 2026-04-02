package com.finance.dashboard.service;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.entity.FinancialRecord;
import com.finance.dashboard.entity.RecordType;
import com.finance.dashboard.entity.User;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;

    // ── Create ────────────────────────────────────────────────────────────────

    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {
        User currentUser = getCurrentUser();

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(currentUser)
                .build();

        return toResponse(recordRepository.save(record));
    }

    // ── Read (with filters + pagination) ─────────────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<FinancialRecordResponse> getRecords(
            RecordFilterRequest filter, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Parse RecordType from string; null means no filter
        RecordType typeFilter = null;
        if (filter.getType() != null && !filter.getType().isBlank()) {
            typeFilter = RecordType.valueOf(filter.getType().toUpperCase());
        }

        Page<FinancialRecord> resultPage = recordRepository.findWithFilters(
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getCategory(),
                typeFilter,
                pageable
        );

        List<FinancialRecordResponse> content = resultPage.getContent()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.<FinancialRecordResponse>builder()
                .content(content)
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .last(resultPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse getRecordById(Long id) {
        return toResponse(findActiveOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public FinancialRecordResponse updateRecord(Long id, FinancialRecordRequest request) {
        FinancialRecord record = findActiveOrThrow(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());

        return toResponse(recordRepository.save(record));
    }

    // ── Soft Delete ───────────────────────────────────────────────────────────

    /**
     * Soft delete: sets deleted=true instead of physically removing the row.
     * This preserves historical data so dashboard analytics remain accurate.
     */
    public void deleteRecord(Long id) {
        FinancialRecord record = findActiveOrThrow(id);
        record.setDeleted(true);
        recordRepository.save(record);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private FinancialRecord findActiveOrThrow(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Financial record not found with id: " + id));
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public FinancialRecordResponse toResponse(FinancialRecord record) {
        return FinancialRecordResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .notes(record.getNotes())
                .createdBy(record.getCreatedBy().getUsername())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
