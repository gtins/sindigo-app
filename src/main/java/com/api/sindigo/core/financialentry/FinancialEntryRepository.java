package com.api.sindigo.core.financialentry;

import com.api.sindigo.core.financialentry.entities.FinancialEntry;
import com.api.sindigo.core.financialentry.entities.FinancialEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialEntryRepository extends JpaRepository<FinancialEntry, UUID> {

    List<FinancialEntry> findByCondominiumId(UUID condominiumId);

    List<FinancialEntry> findByCondominiumIdOrderByDateDesc(UUID condominiumId);

    List<FinancialEntry> findByCondominiumIdAndDateBetweenOrderByDateDesc(UUID condominiumId, LocalDate startDate, LocalDate endDate);

    List<FinancialEntry> findByCondominiumIdAndTypeOrderByDateDesc(UUID condominiumId, FinancialEntryType type);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM FinancialEntry f WHERE f.condominium.id = :condominiumId AND f.type = :type")
    BigDecimal sumByCondominiumIdAndType(@Param("condominiumId") UUID condominiumId, @Param("type") FinancialEntryType type);
}
