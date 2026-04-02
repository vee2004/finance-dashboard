package com.finance.dashboard.service;

import com.finance.dashboard.dto.FinancialRecordRequest;
import com.finance.dashboard.dto.FinancialRecordResponse;
import com.finance.dashboard.dto.PagedResponse;
import com.finance.dashboard.dto.RecordFilterRequest;
import com.finance.dashboard.entity.*;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.repository.FinancialRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("FinancialRecordService Unit Tests")
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @InjectMocks
    private FinancialRecordService recordService;

    private User testUser;
    private FinancialRecord testRecord;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("analyst")
                .email("analyst@finance.com")
                .password("encoded")
                .role(Role.ANALYST)
                .build();

        testRecord = FinancialRecord.builder()
                .id(1L)
                .amount(new BigDecimal("1500.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2024, 3, 15))
                .notes("Monthly salary")
                .createdBy(testUser)
                .deleted(false)
                .build();

        // Stub SecurityContextHolder to return testUser as the authenticated principal
        // Moved to individual tests that need it to avoid UnnecessaryStubbingException
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createRecord: valid request saves and returns response")
    void createRecord_validRequest_returnsResponse() {
        FinancialRecordRequest request = new FinancialRecordRequest();
        request.setAmount(new BigDecimal("1500.00"));
        request.setType(RecordType.INCOME);
        request.setCategory("Salary");
        request.setDate(LocalDate.of(2024, 3, 15));
        request.setNotes("Monthly salary");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        FinancialRecordResponse response = recordService.createRecord(request);

        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(response.getType()).isEqualTo(RecordType.INCOME);
        assertThat(response.getCategory()).isEqualTo("Salary");
        verify(recordRepository, times(1)).save(any(FinancialRecord.class));
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRecordById: existing active record returns response")
    void getRecordById_exists_returnsResponse() {
        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testRecord));

        FinancialRecordResponse response = recordService.getRecordById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getCreatedBy()).isEqualTo("analyst");
    }

    @Test
    @DisplayName("getRecordById: non-existing record throws ResourceNotFoundException")
    void getRecordById_notFound_throwsException() {
        when(recordRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getRecords: filters applied and returns paginated response")
    void getRecords_withFilters_returnsPaginatedResult() {
        Page<FinancialRecord> page = new PageImpl<>(List.of(testRecord),
                PageRequest.of(0, 10), 1);

        when(recordRepository.findWithFilters(any(), any(), any(), any(), any()))
                .thenReturn(page);

        RecordFilterRequest filter = new RecordFilterRequest();
        filter.setType("INCOME");
        PagedResponse<FinancialRecordResponse> result = recordService.getRecords(filter, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    // ── Soft Delete ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteRecord: sets deleted=true without physical removal")
    void deleteRecord_softDelete_setsDeletedTrue() {
        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testRecord));
        when(recordRepository.save(any())).thenReturn(testRecord);

        recordService.deleteRecord(1L);

        assertThat(testRecord.getDeleted()).isTrue();
        verify(recordRepository, never()).deleteById(any());   // ensure no hard delete
        verify(recordRepository, times(1)).save(testRecord);
    }

    @Test
    @DisplayName("deleteRecord: already-deleted record throws ResourceNotFoundException")
    void deleteRecord_alreadyDeleted_throwsException() {
        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.deleteRecord(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateRecord: valid request updates fields and returns response")
    void updateRecord_valid_updatesFields() {
        FinancialRecordRequest update = new FinancialRecordRequest();
        update.setAmount(new BigDecimal("2000.00"));
        update.setType(RecordType.EXPENSE);
        update.setCategory("Rent");
        update.setDate(LocalDate.of(2024, 3, 20));

        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testRecord));
        when(recordRepository.save(any())).thenReturn(testRecord);

        recordService.updateRecord(1L, update);

        assertThat(testRecord.getAmount()).isEqualByComparingTo("2000.00");
        assertThat(testRecord.getCategory()).isEqualTo("Rent");
        verify(recordRepository, times(1)).save(testRecord);
    }
}
