package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.dto.CondominiumCreateDTO;
import com.api.sindigo.core.condominium.dto.CondominiumResponseDTO;
import com.api.sindigo.core.condominium.dto.CondominiumUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/condominiums")
@RequiredArgsConstructor
public class CondominiumController {

    private final CondominiumService condominiumService;

    // CREATE
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<CondominiumResponseDTO> create(@Valid @RequestBody CondominiumCreateDTO dto) {
        CondominiumResponseDTO response = condominiumService.createCondominium(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<CondominiumResponseDTO>> list() {
        List<CondominiumResponseDTO> response = condominiumService.listCondominiums();
        return ResponseEntity.ok(response);
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<CondominiumResponseDTO> getById(@PathVariable UUID id) {
        CondominiumResponseDTO response = condominiumService.getById(id);
        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SINDICO')")
    public ResponseEntity<CondominiumResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody CondominiumUpdateDTO dto) {
        CondominiumResponseDTO response = condominiumService.updateCondominium(id, dto);
        return ResponseEntity.ok(response);
    }
}

