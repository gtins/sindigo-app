package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.dto.BalanceResponseDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryCreateDTO;
import com.api.sindigo.core.financialentry.dto.FinancialEntryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class FinancialEntryController {

    private final FinancialEntryService financialEntryService;

    @PostMapping("/condominiums/{id}/financial-entries")
    public ResponseEntity<FinancialEntryResponseDTO> createFinancialEntry(
            @PathVariable UUID id,
            @Valid @RequestBody FinancialEntryCreateDTO dto
    ) {
        FinancialEntryResponseDTO response = financialEntryService.addFinancialEntry(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/condominiums/{id}/financial-entries")
    public List<FinancialEntryResponseDTO> listFinancialEntries(
            @PathVariable UUID id
    ) {
        return financialEntryService.listByCondominium(id);
    }

    @GetMapping("/condominiums/{id}/balance")
    public ResponseEntity<BalanceResponseDTO> getBalance(
            @PathVariable UUID id
    ) {
        BalanceResponseDTO response = financialEntryService.getBalance(id);
        return ResponseEntity.ok(response);
    }
}
