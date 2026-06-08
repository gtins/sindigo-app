package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.financialentry.dto.BalanceResponseDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import com.api.sindigo.core.financialentry.validator.FinancialEntryValidator;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancialEntryServiceTest {

    private final FinancialEntryRepository financialEntryRepository = mock(FinancialEntryRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final FinancialEntryDtoMapper financialEntryDtoMapper = new FinancialEntryDtoMapper();
    private final FinancialEntryValidator financialEntryValidator = new FinancialEntryValidator();
    private final SecurityContextHelper securityContextHelper = mock(SecurityContextHelper.class);
    private final FinancialEntryService financialEntryService = new FinancialEntryService(
            financialEntryRepository,
            condominiumRepository,
            financialEntryDtoMapper,
            financialEntryValidator,
            securityContextHelper
    );

    private UUID condominiumId;
    private UUID userId;
    private Condominium condominium;
    private User owner;

    @BeforeEach
    void setUp() {
        condominiumId = UUID.randomUUID();
        userId = UUID.randomUUID();
        owner = User.builder().id(userId).build();
        condominium = Condominium.builder()
                .id(condominiumId)
                .owner(owner)
                .name("Residencial Alfa")
                .build();
        
        // Setup padrão
        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(userId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, userId))
                .thenReturn(Optional.of(condominium));
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, userId))
                .thenReturn(Optional.of(condominium));
    }

    @Test
    void addFinancialEntryPersistsAndMapsResponse() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("150.75"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Taxa de condomínio")
                .build();

        when(financialEntryRepository.save(any(FinancialEntry.class))).thenAnswer(invocation -> {
            FinancialEntry entry = invocation.getArgument(0);
            entry.setId(UUID.randomUUID());
            entry.setCreatedAt(LocalDate.of(2026, 6, 1));
            return entry;
        });

        FinancialEntryResponseDTO response = financialEntryService.addFinancialEntry(condominiumId, dto);

        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(FinancialEntryType.INCOME, response.getType());
        assertEquals(new BigDecimal("150.75"), response.getAmount());
        verify(financialEntryRepository).save(any(FinancialEntry.class));
    }

    @Test
    void listByCondominiumReturnsMappedEntries() {
        FinancialEntry entry = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("80.00"))
                .date(LocalDate.of(2026, 6, 2))
                .description("Conta de luz")
                .condominium(condominium)
                .createdAt(LocalDate.of(2026, 6, 2))
                .build();

        when(financialEntryRepository.findByCondominiumId(condominiumId)).thenReturn(List.of(entry));

        List<FinancialEntryResponseDTO> response = financialEntryService.listByCondominium(condominiumId);

        assertEquals(1, response.size());
        assertEquals(entry.getId(), response.getFirst().getId());
        assertEquals("Conta de luz", response.getFirst().getDescription());
    }

    @Test
    void getBalanceCalculatesIncomeExpenseAndNetBalance() {
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.INCOME))
                .thenReturn(new BigDecimal("500.00"));
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.EXPENSE))
                .thenReturn(new BigDecimal("125.00"));

        BalanceResponseDTO response = financialEntryService.getBalance(condominiumId);

        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(new BigDecimal("500.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("125.00"), response.getTotalExpense());
        assertEquals(new BigDecimal("375.00"), response.getNetBalance());
    }

    @Test
    void addFinancialEntryRejectsMissingCondominium() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("25.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Conta de água")
                .build();

        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, userId))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> financialEntryService.addFinancialEntry(condominiumId, dto));
    }

    @Test
    void getBalanceReturnsZeroesWhenNoTransactions() {
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.INCOME))
                .thenReturn(null);
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.EXPENSE))
                .thenReturn(null);

        BalanceResponseDTO response = financialEntryService.getBalance(condominiumId);

        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(BigDecimal.ZERO, response.getTotalIncome());
        assertEquals(BigDecimal.ZERO, response.getTotalExpense());
        assertEquals(BigDecimal.ZERO, response.getNetBalance());
    }

    @Test
    void listByCondominiumReturnsEmptyListWhenNoEntries() {
        when(financialEntryRepository.findByCondominiumId(condominiumId)).thenReturn(List.of());

        List<FinancialEntryResponseDTO> response = financialEntryService.listByCondominium(condominiumId);

        assertEquals(0, response.size());
    }

    @Test
    void listByCondominiumReturnsMultipleEntries() {
        FinancialEntry entry1 = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Taxa 1")
                .condominium(condominium)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();

        FinancialEntry entry2 = FinancialEntry.builder()
                .id(UUID.randomUUID())
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("100.00"))
                .date(LocalDate.of(2026, 6, 2))
                .description("Manutenção")
                .condominium(condominium)
                .createdAt(LocalDate.of(2026, 6, 2))
                .build();

        when(financialEntryRepository.findByCondominiumId(condominiumId)).thenReturn(List.of(entry1, entry2));

        List<FinancialEntryResponseDTO> response = financialEntryService.listByCondominium(condominiumId);

        assertEquals(2, response.size());
    }

    @Test
    void addFinancialEntryWithExpenseType() {
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.EXPENSE)
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.of(2026, 6, 5))
                .description("Conta de água")
                .build();

        when(financialEntryRepository.save(any(FinancialEntry.class))).thenAnswer(invocation -> {
            FinancialEntry entry = invocation.getArgument(0);
            entry.setId(UUID.randomUUID());
            entry.setCreatedAt(LocalDate.of(2026, 6, 5));
            return entry;
        });

        FinancialEntryResponseDTO response = financialEntryService.addFinancialEntry(condominiumId, dto);

        assertEquals(FinancialEntryType.EXPENSE, response.getType());
        assertEquals(new BigDecimal("250.00"), response.getAmount());
    }

    @Test
    void getBalanceCalculatesNegativeNetBalance() {
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.INCOME))
                .thenReturn(new BigDecimal("100.00"));
        when(financialEntryRepository.sumByCondominiumIdAndType(condominiumId, FinancialEntryType.EXPENSE))
                .thenReturn(new BigDecimal("300.00"));

        BalanceResponseDTO response = financialEntryService.getBalance(condominiumId);

        assertEquals(new BigDecimal("100.00"), response.getTotalIncome());
        assertEquals(new BigDecimal("300.00"), response.getTotalExpense());
        assertEquals(new BigDecimal("-200.00"), response.getNetBalance());
    }
}

