package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.validator.ActivityValidator;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CondominiumRepository condominiumRepository;
    private final ActivityValidator activityValidator;

    @Transactional
    public ActivityResponseDTO addActivity(UUID condominiumId, ActivityCreateDTO dto) {
        activityValidator.validateActivityCreation(dto);

        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found with id: " + condominiumId));

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCondominium(condominium);

        Activity saved = activityRepository.save(activity);

        return ActivityDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> listByCondominium(UUID condominiumId) {
        return activityRepository.findByCondominiumId(condominiumId)
                .stream()
                .map(ActivityDtoMapper::toResponseDTO)
                .toList();
    }
}
