package com.api.sindigo.core.attachment;

import com.api.sindigo.core.activityinstance.entities.ActivityInstance;
import com.api.sindigo.core.attachment.dto.AttachmentResponseDTO;
import com.api.sindigo.core.attachment.dto.AttachmentUploadDTO;
import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.attachment.enums.AttachmentCategory;
import com.api.sindigo.core.condominium.entities.Condominium;
import com.api.sindigo.core.provider.entities.Provider;
import com.api.sindigo.core.ticket.entities.Ticket;
import com.api.sindigo.core.ticket.entities.TicketCategory;
import com.api.sindigo.core.ticket.entities.TicketPriority;
import com.api.sindigo.core.ticket.entities.TicketStatus;
import com.api.sindigo.core.user.entities.User;
import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AttachmentServiceTest {

    private static final class OversizedMultipartFile extends MockMultipartFile {
        private final long forcedSize;

        private OversizedMultipartFile(String name, String originalFilename, String contentType, byte[] content, long forcedSize) {
            super(name, originalFilename, contentType, content);
            this.forcedSize = forcedSize;
        }

        @Override
        public long getSize() {
            return forcedSize;
        }
    }

    private final S3Client s3Client = mock(S3Client.class);
    private final S3Presigner s3Presigner = mock(S3Presigner.class);
    private final AttachmentRepository attachmentRepository = mock(AttachmentRepository.class);
    private final AttachmentService attachmentService = new AttachmentService(s3Client, s3Presigner, attachmentRepository);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "bucketName", "sindigo-bucket");
    }

    @Test
    void uploadAndSavePersistsAttachmentAfterS3Upload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento fiscal.pdf",
                "application/pdf",
                "pdf-content".getBytes()
        );
        AttachmentUploadDTO uploadDTO = buildUploadDTO();
        Attachment saved = buildAttachment(uploadDTO.getUploadedBy(), uploadDTO.getCondominium(), uploadDTO.getTicket(), uploadDTO.getActivity(), uploadDTO.getServiceProvider(), uploadDTO.getCategory().getCode());

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(saved);

        Attachment result = attachmentService.uploadAndSave(file, uploadDTO);

        assertEquals(saved, result);
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void uploadAndSaveRejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> attachmentService.uploadAndSave(file, buildUploadDTO()));

        assertEquals("File cannot be empty", exception.getMessage());
        verifyNoInteractions(s3Client, attachmentRepository);
    }

    @Test
    void uploadAndSaveRejectsTooLargeFile() {
        MockMultipartFile file = new OversizedMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                new byte[11],
                11L * 1024 * 1024L
        );

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> attachmentService.uploadAndSave(file, buildUploadDTO()));

        assertEquals("File size exceeds maximum allowed size of 10MB", exception.getMessage());
        verifyNoInteractions(s3Client, attachmentRepository);
    }

    @Test
    void uploadAndSaveRejectsUnsupportedMimeType() {
        MockMultipartFile file = new MockMultipartFile("file", "documento.txt", "text/plain", "content".getBytes());

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> attachmentService.uploadAndSave(file, buildUploadDTO()));

        assertEquals("File type not allowed. Allowed types: PNG, JPG, JPEG, GIF, PDF", exception.getMessage());
        verifyNoInteractions(s3Client, attachmentRepository);
    }

    @Test
    void generatePresignedUrlReturnsUrlForExistingAttachment() throws Exception {
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.INVOICE.getCode());
        attachment.setId(attachmentId);
        attachment.setStorageKey("2026/06/file.pdf");
        attachment.setMimeType("application/pdf");

        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        when(presigned.url()).thenReturn(java.net.URI.create("https://example.com/presigned").toURL());
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

        String url = attachmentService.generatePresignedUrl(attachmentId);

        assertEquals("https://example.com/presigned", url);
    }

    @Test
    void generatePresignedUrlRejectsDeletedAttachment() {
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.INVOICE.getCode());
        attachment.setId(attachmentId);
        attachment.setDeletedAt(LocalDateTime.now());

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> attachmentService.generatePresignedUrl(attachmentId));

        assertEquals("Cannot generate URL for deleted attachment", exception.getMessage());
    }

    @Test
    void getTicketAttachmentsMapsResponses() {
        UUID ticketId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.TICKET_OPENING_EVIDENCE.getCode());
        when(attachmentRepository.findByTicketIdAndDeletedAtNull(ticketId)).thenReturn(List.of(attachment));

        List<AttachmentResponseDTO> result = attachmentService.getTicketAttachments(ticketId);

        assertEquals(1, result.size());
        assertEquals(attachment.getId(), result.getFirst().getId());
        assertEquals(attachment.getOriginalFileName(), result.getFirst().getOriginalFileName());
    }

    @Test
    void getActivityAttachmentsMapsResponses() {
        UUID activityId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.INVOICE.getCode());
        when(attachmentRepository.findByActivityIdAndDeletedAtNull(activityId)).thenReturn(List.of(attachment));

        List<AttachmentResponseDTO> result = attachmentService.getActivityAttachments(activityId);

        assertEquals(1, result.size());
        assertEquals(attachment.getId(), result.getFirst().getId());
    }

    @Test
    void getServiceProviderAttachmentsMapsResponses() {
        UUID providerId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.INVOICE.getCode());
        when(attachmentRepository.findByServiceProviderIdAndDeletedAtNull(providerId)).thenReturn(List.of(attachment));

        List<AttachmentResponseDTO> result = attachmentService.getServiceProviderAttachments(providerId);

        assertEquals(1, result.size());
        assertEquals(attachment.getId(), result.getFirst().getId());
    }

    @Test
    void deleteAttachmentSoftDeletesExistingAttachment() {
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.INVOICE.getCode());
        attachment.setId(attachmentId);

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        attachmentService.deleteAttachment(attachmentId);

        assertNotNull(attachment.getDeletedAt());
        verify(attachmentRepository).save(attachment);
    }

    @Test
    void deleteAttachmentDoesNotSaveAlreadyDeletedAttachment() {
        UUID attachmentId = UUID.randomUUID();
        Attachment attachment = buildAttachment(buildUploadDTO().getUploadedBy(), buildUploadDTO().getCondominium(), buildUploadDTO().getTicket(), buildUploadDTO().getActivity(), buildUploadDTO().getServiceProvider(), AttachmentCategory.INVOICE.getCode());
        attachment.setId(attachmentId);
        attachment.setDeletedAt(LocalDateTime.now());

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(attachmentId);

        verify(attachmentRepository).findById(attachmentId);
        verifyNoMoreInteractions(attachmentRepository);
    }

    @Test
    void generatePresignedUrlThrowsNotFoundForMissingAttachment() {
        UUID attachmentId = UUID.randomUUID();
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> attachmentService.generatePresignedUrl(attachmentId));

        assertEquals("Attachment not found with ID: " + attachmentId, exception.getMessage());
    }

    private AttachmentUploadDTO buildUploadDTO() {
        Condominium condominium = Condominium.builder().id(UUID.randomUUID()).build();
        Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .title("Chamado")
                .description("Descrição")
                .location("Local")
                .status(TicketStatus.ABERTO)
                .category(TicketCategory.MANUTENCAO)
                .priority(TicketPriority.ALTA)
                .condominium(condominium)
                .createdBy(User.builder().id(UUID.randomUUID()).build())
                .activities(List.of())
                .build();
        Provider provider = Provider.builder().id(UUID.randomUUID()).condominium(condominium).activities(List.of()).build();
        ActivityInstance activity = ActivityInstance.builder().id(UUID.randomUUID()).activity(null).build();
        User uploadedBy = User.builder().id(UUID.randomUUID()).build();
        return AttachmentUploadDTO.builder()
                .condominium(condominium)
                .ticket(ticket)
                .activity(activity)
                .serviceProvider(provider)
                .uploadedBy(uploadedBy)
                .category(AttachmentCategory.INVOICE)
                .build();
    }

    private Attachment buildAttachment(User uploadedBy, Condominium condominium, Ticket ticket, ActivityInstance activity, Provider provider, String category) {
        return Attachment.builder()
                .id(UUID.randomUUID())
                .condominium(condominium)
                .ticket(ticket)
                .activity(activity)
                .serviceProvider(provider)
                .uploadedBy(uploadedBy)
                .attachmentCategory(category)
                .fileType("PDF")
                .originalFileName("file.pdf")
                .storageKey("2026/06/file.pdf")
                .mimeType("application/pdf")
                .sizeBytes(1024L)
                .createdAt(LocalDateTime.of(2026, 6, 1, 12, 0))
                .build();
    }
}

