package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.dto.BalanceResponseDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinancialEntryControllerTest {

    private final FinancialEntryService financialEntryService = mock(FinancialEntryService.class);
    private final FinancialEntryController controller = new FinancialEntryController(financialEntryService);

    @Test
    void createFinancialEntryReturnsCreatedResponse() {
        UUID condominiumId = UUID.randomUUID();
        FinancialEntryCreateDTO dto = FinancialEntryCreateDTO.builder()
                .type(FinancialEntryType.INCOME)
                .amount(new BigDecimal("150.00"))
                .date(LocalDate.of(2026, 6, 1))
                .description("Taxa condominial")
                .build();
        FinancialEntryResponseDTO response = buildEntryResponse(condominiumId, dto.getType(), dto.getAmount(), dto.getDate(), dto.getDescription());

        when(financialEntryService.addFinancialEntry(condominiumId, dto)).thenReturn(response);

        var result = controller.createFinancialEntry(condominiumId, dto);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(financialEntryService).addFinancialEntry(condominiumId, dto);
    }

    @Test
    void listFinancialEntriesReturnsList() {
        UUID condominiumId = UUID.randomUUID();
        FinancialEntryResponseDTO response = buildEntryResponse(condominiumId, FinancialEntryType.INCOME, new BigDecimal("150.00"), LocalDate.of(2026, 6, 1), "Taxa condominial");
        when(financialEntryService.listByCondominium(condominiumId)).thenReturn(List.of(response));

        var result = controller.listFinancialEntries(condominiumId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
    }

    @Test
    void getBalanceReturnsBalanceResponse() {
        UUID condominiumId = UUID.randomUUID();
        BalanceResponseDTO balance = BalanceResponseDTO.builder()
                .condominiumId(condominiumId)
                .totalIncome(new BigDecimal("500.00"))
                .totalExpense(new BigDecimal("200.00"))
                .netBalance(new BigDecimal("300.00"))
                .build();

        when(financialEntryService.getBalance(condominiumId)).thenReturn(balance);

        var result = controller.getBalance(condominiumId);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(balance, result.getBody());
    }

    private FinancialEntryResponseDTO buildEntryResponse(UUID condominiumId, FinancialEntryType type, BigDecimal amount, LocalDate date, String description) {
        return FinancialEntryResponseDTO.builder()
                .id(UUID.randomUUID())
                .condominiumId(condominiumId)
                .type(type)
                .amount(amount)
                .date(date)
                .description(description)
                .createdAt(LocalDate.of(2026, 6, 1))
                .build();
    }
}

