package com.api.sindigo.core.attachment;

import com.api.sindigo.core.activityinstance.ActivityInstanceRepository;
import com.api.sindigo.core.attachment.dto.AttachmentResponseDTO;
import com.api.sindigo.core.attachment.dto.AttachmentUploadDTO;
import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.attachment.enums.AttachmentCategory;
import com.api.sindigo.core.ticket.TicketRepository;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.user.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private static final String ERROR_RESPONSE_KEY = "error";
    private static final String URL_RESPONSE_KEY = "url";

    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found";
    private static final String ACTIVITY_ID_REQUIRED_MESSAGE = "Activity ID is required";
    private static final String ACTIVITY_WITHOUT_TICKET_MESSAGE = "Activity does not have associated ticket";
    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error";
    private static final String UPLOAD_ERROR_PREFIX = "Error uploading file: ";

    private final AttachmentService attachmentService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final ActivityInstanceRepository activityInstanceRepository;

    @PostMapping("/ticket/{ticketId}/upload")
    public ResponseEntity<Object> uploadTicketAttachment(
            @PathVariable UUID ticketId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "ticket_opening_evidence") String category,
            Authentication authentication) {
        try {
            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IllegalArgumentException(TICKET_NOT_FOUND_MESSAGE));

            User currentUser = extractUserFromAuthentication(authentication);
            AttachmentCategory attachmentCategory = AttachmentCategory.fromCode(category);

            AttachmentUploadDTO uploadDTO = AttachmentUploadDTO.builder()
                    .condominium(ticket.getCondominium())
                    .ticket(ticket)
                    .uploadedBy(currentUser)
                    .category(attachmentCategory)
                    .build();

            Attachment attachment = attachmentService.uploadAndSave(file, uploadDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(attachment));
        } catch (IOException e) {
            log.error("Error uploading ticket attachment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorBody(UPLOAD_ERROR_PREFIX + e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading ticket attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorBody(e.getMessage()));
        }
    }

    @PostMapping("/activity/{activityId}/upload")
    public ResponseEntity<Object> uploadActivityAttachment(
            @PathVariable UUID activityId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "invoice") String category,
            Authentication authentication) {
        try {
            if (activityId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(buildErrorBody(ACTIVITY_ID_REQUIRED_MESSAGE));
            }

            var activity = activityInstanceRepository.findById(activityId)
                    .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

            User currentUser = extractUserFromAuthentication(authentication);

            var activityDefinition = activity.getActivity();
            if (activityDefinition == null || activityDefinition.getTicket() == null) {
                throw new IllegalArgumentException(ACTIVITY_WITHOUT_TICKET_MESSAGE);
            }

            AttachmentCategory attachmentCategory = AttachmentCategory.fromCode(category);

            AttachmentUploadDTO uploadDTO = AttachmentUploadDTO.builder()
                    .condominium(activityDefinition.getCondominium())
                    .activity(activity)
                    .serviceProvider(activityDefinition.getProvider())
                    .uploadedBy(currentUser)
                    .category(attachmentCategory)
                    .build();

            Attachment attachment = attachmentService.uploadAndSave(file, uploadDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(attachment));
        } catch (IOException e) {
            log.error("Error uploading activity attachment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorBody(UPLOAD_ERROR_PREFIX + e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading activity attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildErrorBody(e.getMessage()));
        }
    }

    @GetMapping("/{attachmentId}/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@PathVariable UUID attachmentId) {
        try {
            String presignedUrl = attachmentService.generatePresignedUrl(attachmentId);
            return ResponseEntity.ok(Map.of(URL_RESPONSE_KEY, presignedUrl));
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorBody(e.getMessage()));
        }
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<AttachmentResponseDTO>> getTicketAttachments(@PathVariable UUID ticketId) {
        try {
            List<AttachmentResponseDTO> attachments = attachmentService.getTicketAttachments(ticketId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            log.error("Error fetching ticket attachments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<AttachmentResponseDTO>> getActivityAttachments(@PathVariable UUID activityId) {
        try {
            List<AttachmentResponseDTO> attachments = attachmentService.getActivityAttachments(activityId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            log.error("Error fetching activity attachments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<AttachmentResponseDTO>> getServiceProviderAttachments(@PathVariable UUID providerId) {
        try {
            List<AttachmentResponseDTO> attachments = attachmentService.getServiceProviderAttachments(providerId);
            return ResponseEntity.ok(attachments);
        } catch (Exception e) {
            log.error("Error fetching service provider attachments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Object> deleteAttachment(@PathVariable UUID attachmentId) {
        try {
            attachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting attachment", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorBody(e.getMessage()));
        }
    }

    private User extractUserFromAuthentication(Authentication authentication) {
        String identifier = authentication.getName();

        Optional<UUID> userId = parseUuid(identifier);

        if (userId.isPresent()) {
            return userRepository.findById(userId.get())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId.get()));
        }

        return userRepository.findByEmail(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + identifier));
    }

    private Optional<UUID> parseUuid(String identifier) {
        try {
            return Optional.of(UUID.fromString(identifier));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Map<String, String> buildErrorBody(String message) {
        return Map.of(ERROR_RESPONSE_KEY, message != null ? message : UNEXPECTED_ERROR_MESSAGE);
    }

    private AttachmentResponseDTO convertToDTO(Attachment attachment) {
        return AttachmentResponseDTO.builder()
                .id(attachment.getId())
                .originalFileName(attachment.getOriginalFileName())
                .fileType(attachment.getFileType())
                .mimeType(attachment.getMimeType())
                .sizeBytes(attachment.getSizeBytes())
                .attachmentCategory(attachment.getAttachmentCategory())
                .uploadedBy(attachment.getUploadedBy().getId())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}