package com.api.sindigo.core.provider;

import com.api.sindigo.core.provider.dto.ProviderCreateDTO;
import com.api.sindigo.core.provider.dto.ProviderResponseDTO;
import com.api.sindigo.core.provider.dto.ProviderUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums/{condominiumId}/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final ProviderService providerService;

    @PostMapping
    public ResponseEntity<ProviderResponseDTO> createProvider(
            @PathVariable UUID condominiumId,
            @Valid @RequestBody ProviderCreateDTO dto) {
        ProviderResponseDTO response = providerService.createProvider(condominiumId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ProviderResponseDTO>> getProviders(@PathVariable UUID condominiumId) {
        List<ProviderResponseDTO> response = providerService.getProvidersByCondominium(condominiumId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderResponseDTO> getProviderById(
            @PathVariable UUID condominiumId,
            @PathVariable UUID providerId) {
        ProviderResponseDTO response = providerService.getProviderById(condominiumId, providerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{providerId}")
    public ResponseEntity<ProviderResponseDTO> updateProvider(
            @PathVariable UUID condominiumId,
            @PathVariable UUID providerId,
            @Valid @RequestBody ProviderUpdateDTO dto) {
        ProviderResponseDTO response = providerService.updateProvider(condominiumId, providerId, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{providerId}")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable UUID condominiumId,
            @PathVariable UUID providerId) {
        providerService.deleteProvider(condominiumId, providerId);
        return ResponseEntity.noContent().build();
    }
}

