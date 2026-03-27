package com.api.sindigo.core.condominium;

import com.api.sindigo.core.condominium.entities.Condominium;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CondominiumRepository extends JpaRepository<Condominium, UUID> {
}

