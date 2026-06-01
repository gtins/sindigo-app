package com.api.sindigo.core.activity;

import com.api.sindigo.core.activity.dto.ActivityCloseDTO;
import com.api.sindigo.core.activity.dto.ActivityCreateDTO;
import com.api.sindigo.core.activity.dto.ActivityResponseDTO;
import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityStatus;
import com.api.sindigo.core.activity.entities.ActivityType;
import com.api.sindigo.core.activity.validator.ActivityValidator;
import com.api.sindigo.core.auth.security.SecurityContextHelper;
import com.api.sindigo.core.condominium.CondominiumRepository;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.ProviderRepository;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.TicketRepository;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ActivityServiceTest {

    private final ActivityRepository activityRepository = mock(ActivityRepository.class);
    private final CondominiumRepository condominiumRepository = mock(CondominiumRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final ProviderRepository providerRepository = mock(ProviderRepository.class);
    private final ActivityValidator activityValidator = new ActivityValidator();
    private final SecurityContextHelper securityContextHelper = mock(SecurityContextHelper.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ActivityService activityService = new ActivityService(
            activityRepository,
            condominiumRepository,
            ticketRepository,
            providerRepository,
            activityValidator,
            securityContextHelper,
            userRepository
    );

    private UUID authenticatedUserId;
    private UUID condominiumId;
    private Condominium condominium;
    private User creator;

    @BeforeEach
    void setUp() {
        authenticatedUserId = UUID.randomUUID();
        condominiumId = UUID.randomUUID();
        creator = User.builder()
                .id(authenticatedUserId)
                .name("Maria Souza")
                .email("maria@example.com")
                .build();
        condominium = Condominium.builder()
                .id(condominiumId)
                .name("Residencial Alfa")
                .owner(creator)
                .build();
    }

    @Test
    void addActivityPersistsManualActivityWhenNoRelationsAreProvided() {
        ActivityCreateDTO dto = new ActivityCreateDTO(
                "Reunião",
                "Reunião ordinária do condomínio",
                ActivityType.ONCE,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                null,
                null
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(creator));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(UUID.randomUUID());
            return activity;
        });

        ActivityResponseDTO response = activityService.addActivity(condominiumId, dto);

        assertEquals(ActivityStatus.PENDING, response.getStatus());
        assertEquals(ActivityOrigin.MANUAL, response.getOrigin());
        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(authenticatedUserId, response.getCreatedById());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void addActivityPermitsActivityStartingAndEndingOnSameDay() {
        ActivityCreateDTO dto = new ActivityCreateDTO(
                "Reunião de uma hora",
                "Reunião rápida do condomínio",
                ActivityType.ONCE,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 1),
                null,
                null
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(creator));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(UUID.randomUUID());
            return activity;
        });

        ActivityResponseDTO response = activityService.addActivity(condominiumId, dto);

        assertEquals(ActivityStatus.PENDING, response.getStatus());
        assertEquals(ActivityOrigin.MANUAL, response.getOrigin());
        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(authenticatedUserId, response.getCreatedById());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void addActivityPersistsActivityLinkedToTicketAndProvider() {
        UUID ticketId = UUID.randomUUID();
        UUID providerId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
                .id(ticketId)
                .title("Vazamento")
                .description("Vazamento no banheiro social")
                .location("Bloco A")
                .status(TicketStatus.ABERTO)
                .category(TicketCategory.MANUTENCAO)
                .priority(TicketPriority.ALTA)
                .condominium(condominium)
                .createdBy(creator)
                .activities(Collections.emptyList())
                .build();
        Provider provider = Provider.builder()
                .id(providerId)
                .name("Carlos Manutenção")
                .serviceType("Elétrica")
                .condominium(condominium)
                .activities(Collections.emptyList())
                .build();
        ActivityCreateDTO dto = new ActivityCreateDTO(
                "Reparo elétrico",
                "Troca de disjuntor",
                ActivityType.ONCE,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 3),
                ticketId,
                providerId
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(userRepository.findById(authenticatedUserId)).thenReturn(Optional.of(creator));
        when(ticketRepository.findByIdAndCondominiumId(ticketId, condominiumId)).thenReturn(Optional.of(ticket));
        when(providerRepository.findByIdAndCondominiumId(providerId, condominiumId)).thenReturn(Optional.of(provider));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.setId(UUID.randomUUID());
            return activity;
        });

        ActivityResponseDTO response = activityService.addActivity(condominiumId, dto);

        assertEquals(ActivityStatus.PENDING, response.getStatus());
        assertEquals(ActivityOrigin.CHAMADO, response.getOrigin());
        assertEquals(ticketId, response.getTicketId());
        assertEquals(providerId, response.getProviderId());
    }

    @Test
    void listByCondominiumReturnsMappedActivities() {
        Activity activity = Activity.builder()
                .id(UUID.randomUUID())
                .title("Reunião")
                .description("Reunião ordinária")
                .type(ActivityType.ONCE)
                .origin(ActivityOrigin.MANUAL)
                .status(ActivityStatus.PENDING)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 2))
                .condominium(condominium)
                .createdBy(creator)
                .instances(Collections.emptyList())
                .build();

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndUserHasAccess(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(activityRepository.findByCondominiumId(condominiumId)).thenReturn(List.of(activity));

        List<ActivityResponseDTO> response = activityService.listByCondominium(condominiumId);

        assertEquals(1, response.size());
        assertEquals(activity.getId(), response.getFirst().getId());
        assertEquals("Reunião", response.getFirst().getTitle());
    }

    @Test
    void closeActivitySetsStatusClosedAtAndAppendsClosingNotes() {
        UUID activityId = UUID.randomUUID();
        Activity activity = Activity.builder()
                .id(activityId)
                .title("Reunião")
                .description("Reunião ordinária")
                .type(ActivityType.ONCE)
                .origin(ActivityOrigin.MANUAL)
                .status(ActivityStatus.PENDING)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 2))
                .condominium(condominium)
                .createdBy(creator)
                .instances(Collections.emptyList())
                .build();
        ActivityCloseDTO dto = new ActivityCloseDTO(ActivityStatus.COMPLETED, "Atividade concluída");

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.of(condominium));
        when(activityRepository.findByIdAndCondominiumId(activityId, condominiumId)).thenReturn(Optional.of(activity));
        when(activityRepository.save(any(Activity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponseDTO response = activityService.closeActivity(condominiumId, activityId, dto);

        assertEquals(ActivityStatus.COMPLETED, response.getStatus());
        assertNotNull(response.getClosedAt());
        assertEquals(activityId, response.getId());
        assertEquals(condominiumId, response.getCondominiumId());
        assertEquals(creator.getId(), response.getCreatedById());
    }

    @Test
    void addActivityRejectsWhenCondominiumIsNotAccessible() {
        ActivityCreateDTO dto = new ActivityCreateDTO(
                "Reunião",
                "Reunião ordinária do condomínio",
                ActivityType.ONCE,
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 2),
                null,
                null
        );

        when(securityContextHelper.getAuthenticatedUserId()).thenReturn(authenticatedUserId);
        when(condominiumRepository.findByIdAndOwnerId(condominiumId, authenticatedUserId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> activityService.addActivity(condominiumId, dto));
    }
}


