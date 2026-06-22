package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCloseDTO;
import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityStatus;
import com.api.sindigo.core.activity.validator.ActivityValidator;
import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.ProviderRepository;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.TicketRepository;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private static final String CONDOMINIUM_ACCESS_ERROR_MESSAGE =
            "Condominio não encontrado ou voce não tem acesso!";

    private static final String AUTHENTICATED_USER_NOT_FOUND_MESSAGE =
            "Usuário autenticado não encontrado!";

    private static final String TICKET_NOT_FOUND_MESSAGE = "Chamado não encontrado";
    private static final String PROVIDER_NOT_FOUND_MESSAGE = "Provedor não encontrado";
    private static final String ACTIVITY_NOT_FOUND_MESSAGE = "Atividade não encontrada";
    private static final DateTimeFormatter CLOSING_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

        Condominium condominium = condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        User creator = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalStateException(AUTHENTICATED_USER_NOT_FOUND_MESSAGE));

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setType(dto.getType());
        activity.setOrigin(dto.getOrigin() != null ? dto.getOrigin() : ActivityOrigin.MANUAL);
        activity.setStatus(ActivityStatus.PENDING);
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setCondominium(condominium);
        activity.setCreatedBy(creator);

        if (dto.getTicketId() != null) {
            Ticket ticket = ticketRepository.findByIdAndCondominiumId(dto.getTicketId(), condominiumId)
                    .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND_MESSAGE));
            activity.setTicket(ticket);
            activity.setOrigin(ActivityOrigin.CHAMADO);
        }

        if (dto.getProviderId() != null) {
            Provider provider = providerRepository.findByIdAndCondominiumId(dto.getProviderId(), condominiumId)
                    .orElseThrow(() -> new IllegalArgumentException(PROVIDER_NOT_FOUND_MESSAGE));
            activity.setProvider(provider);
        }

        Activity saved = activityRepository.save(activity);

        return ActivityDtoMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> listByCondominium(UUID condominiumId) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        return activityRepository.findByCondominiumId(condominiumId)
                .stream()
                .map(ActivityDtoMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ActivityResponseDTO closeActivity(UUID condominiumId, UUID activityId, ActivityCloseDTO dto) {
        UUID authenticatedUserId = securityContextHelper.getAuthenticatedUserId();

        condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONDOMINIUM_ACCESS_ERROR_MESSAGE));

        Activity activity = activityRepository.findByIdAndCondominiumId(activityId, condominiumId)
                .orElseThrow(() -> new IllegalArgumentException(ACTIVITY_NOT_FOUND_MESSAGE));

        activity.setStatus(dto.getStatus());
        activity.setClosedAt(LocalDateTime.now());

        if (dto.getClosingNotes() != null && !dto.getClosingNotes().isBlank()) {
            String existingDescription = activity.getDescription() != null ? activity.getDescription() : "";
            String timestamp = LocalDateTime.now().format(CLOSING_DATE_FORMATTER);

            activity.setDescription(
                    existingDescription
                            + (existingDescription.isEmpty() ? "" : "\n\n")
                            + "[Encerrada em "
                            + timestamp
                            + "] "
                            + dto.getClosingNotes()
            );
        }

        Activity closedActivity = activityRepository.save(activity);
        return ActivityDtoMapper.toResponseDTO(closedActivity);
    }
}