package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.validator.ActivityValidator;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.provider.ProviderRepository;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.TicketRepository;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
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
    private final TicketRepository ticketRepository;
    private final ProviderRepository providerRepository;
    private final ActivityValidator activityValidator;
    private final SecurityContextHelper securityContextHelper;
    private final UserRepository userRepository;

    @Transactional
    public ActivityResponseDTO addActivity(UUID condominiumId, ActivityCreateDTO dto) {
        activityValidator.validateActivityCreation(dto);

        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        // Verifica se o condomínio pertence ao usuário autenticado
        Condominium condominium = condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        User creator = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setOrigin(dto.getOrigin() != null ? dto.getOrigin() : ActivityOrigin.MANUAL);
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCondominium(condominium);
        activity.setCreatedBy(creator);

        // Vincular ticket se fornecido
        if (dto.getTicketId() != null) {
            Ticket ticket = ticketRepository.findByIdAndCondominiumId(dto.getTicketId(), condominiumId)
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
            activity.setTicket(ticket);
            activity.setOrigin(ActivityOrigin.CHAMADO);
        }

        // Vincular prestador se fornecido
        if (dto.getProviderId() != null) {
            Provider provider = providerRepository.findByIdAndCondominiumId(dto.getProviderId(), condominiumId)
                    .orElseThrow(() -> new IllegalArgumentException("Provider not found"));
            activity.setProvider(provider);
        }

        Activity saved = activityRepository.save(activity);

        return ActivityDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> listByCondominium(UUID condominiumId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        // Verifica se o condomínio pertence ao usuário autenticado
        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Condominium not found or you don't have access"));

        return activityRepository.findByCondominiumIdAndCreatedById(condominiumId, authenticatedUserId)
                .stream()
                .map(ActivityDtoMapper::toResponseDTO)
                .toList();
    }
}
