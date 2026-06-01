package com.api.sindigo.core.attachment;

import com.api.sindigo.core.attachment.dto.AttachmentResponseDTO;
import com.api.sindigo.core.ticket.TicketRepository;
import com.api.sindigo.core.activityinstance.ActivityInstanceRepository;
import com.api.sindigo.core.user.UserRepository;
import com.api.sindigo.core.attachment.dto.AttachmentUploadDTO;
import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.core.attachment.enums.AttachmentCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final ActivityInstanceRepository activityInstanceRepository;

    /**
     * Upload de arquivo para um chamado (abertura ou fechamento)
     * POST /api/v1/attachments/ticket/{ticketId}/upload
     */
    @PostMapping("/ticket/{ticketId}/upload")
    public ResponseEntity<?> uploadTicketAttachment(
            @PathVariable UUID ticketId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "ticket_opening_evidence") String category,
            Authentication authentication) {
        try {
            var ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
            
            // Extrair usuário do authentication - tenta UUID primeiro, depois email
            var currentUser = extractUserFromAuthentication(authentication);
            
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
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading ticket attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Upload de arquivo para uma atividade (nota fiscal do prestador)
     * POST /api/v1/attachments/activity/{activityId}/upload
     */
    @PostMapping("/activity/{activityId}/upload")
    public ResponseEntity<?> uploadActivityAttachment(
            @PathVariable UUID activityId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "invoice") String category,
            Authentication authentication) {
        try {
            if (activityId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Activity ID is required"));
            }
            
            var activity = activityInstanceRepository.findById(activityId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Activity not found with ID: " + activityId));
            
            // Extrair usuário do authentication - tenta UUID primeiro, depois email
            var currentUser = extractUserFromAuthentication(authentication);
            
            // Obter Ticket via Activity
            var activityDefinition = activity.getActivity();
            if (activityDefinition == null || activityDefinition.getTicket() == null) {
                throw new IllegalArgumentException("Activity does not have associated ticket");
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
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error uploading activity attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{attachmentId}/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@PathVariable UUID attachmentId) {
        try {
            String presignedUrl = attachmentService.generatePresignedUrl(attachmentId);
            return ResponseEntity.ok(Map.of("url", presignedUrl));
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
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
    public ResponseEntity<?> deleteAttachment(@PathVariable UUID attachmentId) {
        try {
            attachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting attachment", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============ Helper Methods ============

    private com.api.sindigo.core.user.entities.User extractUserFromAuthentication(Authentication authentication) {
        String identifier = authentication.getName();
        
        // Tenta primeiro como UUID
        try {
            UUID userId = UUID.fromString(identifier);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        } catch (IllegalArgumentException e) {
            // Não é UUID, tenta como email
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + identifier));
        }
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

