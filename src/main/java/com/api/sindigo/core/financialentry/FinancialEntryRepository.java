package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, UUID> {

    List<FinancialEntry> findByCondominiumId(UUID condominiumId);
}
