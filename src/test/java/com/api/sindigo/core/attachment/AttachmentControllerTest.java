package com.api.sindigo.core.attachment;

import com.api.sindigo.core.activity.entities.Activity;
import com.api.sindigo.core.activity.entities.ActivityOrigin;
import com.api.sindigo.core.activity.entities.ActivityStatus;
import com.api.sindigo.core.activity.entities.ActivityType;
import com.api.sindigo.core.activityinstance.entities.ActivityInstance;
import com.api.sindigo.core.attachment.dto.AttachmentResponseDTO;
import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.attachment.enums.AttachmentCategory;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.TicketRepository;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttachmentControllerTest {

    private final AttachmentService attachmentService = mock(AttachmentService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final com.api.sindigo.core.activityinstance.ActivityInstanceRepository activityInstanceRepository = mock(com.api.sindigo.core.activityinstance.ActivityInstanceRepository.class);
    private final AttachmentController controller = new AttachmentController(
            attachmentService,
            userRepository,
            ticketRepository,
            activityInstanceRepository
    );

    @Test
    void uploadTicketAttachmentReturnsCreatedResponse() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId.toString(), "token");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evidence.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        Condominium condominium = Condominium.builder().id(UUID.randomUUID()).build();
        Ticket ticket = buildTicket(ticketId, condominium);
        User uploader = User.builder().id(userId).build();
        Attachment saved = buildAttachment(uploader, condominium, ticket, null, null, AttachmentCategory.TICKET_OPENING_EVIDENCE.getCode());

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(userId)).thenReturn(Optional.of(uploader));
        when(attachmentService.uploadAndSave(eq(file), any())).thenReturn(saved);

        var result = controller.uploadTicketAttachment(ticketId, file, "ticket_opening_evidence", authentication);

        assertEquals(201, result.getStatusCode().value());
        AttachmentResponseDTO body = (AttachmentResponseDTO) result.getBody();
        assertNotNull(body);
        assertEquals(saved.getId(), body.getId());
        assertEquals(saved.getOriginalFileName(), body.getOriginalFileName());
        verify(attachmentService).uploadAndSave(eq(file), any());
    }

    @Test
    void uploadActivityAttachmentReturnsCreatedResponseUsingEmailAuthentication() throws Exception {
        UUID activityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Authentication authentication = new UsernamePasswordAuthenticationToken("user@example.com", "token");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );

        Condominium condominium = Condominium.builder().id(UUID.randomUUID()).build();
        Ticket ticket = buildTicket(UUID.randomUUID(), condominium);
        Provider provider = Provider.builder().id(UUID.randomUUID()).condominium(condominium).activities(List.of()).build();
        Activity activityDefinition = Activity.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .ticket(ticket)
                .provider(provider)
                .status(ActivityStatus.PENDING)
                .type(ActivityType.ONCE)
                .origin(ActivityOrigin.CHAMADO)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 2))
                .createdBy(User.builder().id(userId).build())
                .build();
        ActivityInstance activityInstance = ActivityInstance.builder()
                .id(activityId)
                .activity(activityDefinition)
                .status(ActivityStatus.PENDING)
                .scheduledAt(LocalDate.of(2026, 6, 1))
                .build();
        User uploader = User.builder().id(userId).email("user@example.com").build();
        Attachment saved = buildAttachment(uploader, condominium, null, activityInstance, provider, AttachmentCategory.INVOICE.getCode());

        when(activityInstanceRepository.findById(activityId)).thenReturn(Optional.of(activityInstance));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(uploader));
        when(attachmentService.uploadAndSave(eq(file), any())).thenReturn(saved);

        var result = controller.uploadActivityAttachment(activityId, file, "invoice", authentication);

        assertEquals(201, result.getStatusCode().value());
        AttachmentResponseDTO body = (AttachmentResponseDTO) result.getBody();
        assertNotNull(body);
        assertEquals(saved.getId(), body.getId());
    }

    @Test
    void getPresignedUrlReturnsUrlMap() {
        UUID attachmentId = UUID.randomUUID();
        when(attachmentService.generatePresignedUrl(attachmentId)).thenReturn("https://example.com/presigned");

        var result = controller.getPresignedUrl(attachmentId);

        assertEquals(200, result.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) result.getBody();
        assertNotNull(body);
        assertEquals("https://example.com/presigned", body.get("url"));
    }

    @Test
    void getTicketAttachmentsReturnsList() {
        UUID ticketId = UUID.randomUUID();
        AttachmentResponseDTO response = buildAttachmentResponse();
        when(attachmentService.getTicketAttachments(ticketId)).thenReturn(List.of(response));

        var result = controller.getTicketAttachments(ticketId);

        assertEquals(200, result.getStatusCode().value());
        List<AttachmentResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(response, body.getFirst());
    }

    @Test
    void getActivityAttachmentsReturnsList() {
        UUID activityId = UUID.randomUUID();
        AttachmentResponseDTO response = buildAttachmentResponse();
        when(attachmentService.getActivityAttachments(activityId)).thenReturn(List.of(response));

        var result = controller.getActivityAttachments(activityId);

        assertEquals(200, result.getStatusCode().value());
        List<AttachmentResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(response, body.getFirst());
    }

    @Test
    void getServiceProviderAttachmentsReturnsList() {
        UUID providerId = UUID.randomUUID();
        AttachmentResponseDTO response = buildAttachmentResponse();
        when(attachmentService.getServiceProviderAttachments(providerId)).thenReturn(List.of(response));

        var result = controller.getServiceProviderAttachments(providerId);

        assertEquals(200, result.getStatusCode().value());
        List<AttachmentResponseDTO> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(response, body.getFirst());
    }

    @Test
    void deleteAttachmentReturnsNoContent() {
        UUID attachmentId = UUID.randomUUID();

        var result = controller.deleteAttachment(attachmentId);

        assertEquals(204, result.getStatusCode().value());
        verify(attachmentService).deleteAttachment(attachmentId);
    }

    private Ticket buildTicket(UUID ticketId, Condominium condominium) {
        return Ticket.builder()
                .id(ticketId)
                .title("Vazamento")
                .description("Vazamento no banheiro social")
                .location("Bloco A")
                .status(TicketStatus.ABERTO)
                .category(TicketCategory.MANUTENCAO)
                .priority(TicketPriority.ALTA)
                .condominium(condominium)
                .createdBy(User.builder().id(UUID.randomUUID()).build())
                .activities(List.of())
                .build();
    }

    private Attachment buildAttachment(User uploadedBy, Condominium condominium, Ticket ticket, ActivityInstance activity, Provider provider, String category) {
        return Attachment.builder()
                .id(UUID.randomUUID())
                .originalFileName("file.pdf")
                .fileType("PDF")
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .attachmentCategory(category)
                .uploadedBy(uploadedBy)
                .condominium(condominium)
                .ticket(ticket)
                .activity(activity)
                .serviceProvider(provider)
                .storageKey("2026/06/file.pdf")
                .createdAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();
    }

    private AttachmentResponseDTO buildAttachmentResponse() {
        return AttachmentResponseDTO.builder()
                .id(UUID.randomUUID())
                .originalFileName("file.pdf")
                .fileType("PDF")
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .attachmentCategory("ticket_opening_evidence")
                .uploadedBy(UUID.randomUUID())
                .createdAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();
    }
}


