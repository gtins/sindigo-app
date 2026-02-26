package com.api.sindigo.core.building;

import com.api.sindigo.core.building.entities.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
}
