package com.api.sindigo.core.attachment;

import com.api.sindigo.core.attachment.dto.AttachmentResponseDTO;
import com.api.sindigo.core.attachment.dto.AttachmentUploadDTO;
import com.api.sindigo.core.attachment.entities.Attachment;
import com.api.sindigo.exception.BusinessRuleException;
import com.api.sindigo.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AttachmentRepository attachmentRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif",
            "application/pdf"
    );
    private static final String ERROR_UPLOADING_S3 = "Error uploading file to S3";
    private static final String ERROR_GENERATING_URL = "Error generating presigned URL";
    private static final String ERROR_FILE_EMPTY = "File cannot be empty";
    private static final String ERROR_FILE_SIZE = "File size exceeds maximum allowed size of 10MB";
    private static final String ERROR_FILE_TYPE = "File type not allowed. Allowed types: PNG, JPG, JPEG, GIF, PDF";

    /**
     * Upload arquivo para S3 e salva metadados no banco
     */
    @Transactional
    public Attachment uploadAndSave(MultipartFile file, AttachmentUploadDTO uploadDTO) throws IOException {
        validateFile(file);

        String storageKey = generateStorageKey(file);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // 1. Upload para S3
        uploadToS3(file, storageKey, contentType);

        // 2. Criar entidade Attachment
        Attachment attachment = Attachment.builder()
                .condominium(uploadDTO.getCondominium())
                .ticket(uploadDTO.getTicket())
                .activity(uploadDTO.getActivity())
                .serviceProvider(uploadDTO.getServiceProvider())
                .uploadedBy(uploadDTO.getUploadedBy())
                .attachmentCategory(uploadDTO.getCategory().getCode())
                .fileType(getFileType(file.getOriginalFilename()))
                .originalFileName(file.getOriginalFilename())
                .storageKey(storageKey)
                .mimeType(contentType)
                .sizeBytes(file.getSize())
                .build();

        Attachment saved = attachmentRepository.save(attachment);
        log.info("Attachment uploaded successfully. ID: {}, S3 Key: {}", saved.getId(), storageKey);
        return saved;
    }

    /**
     * Gera URL assinada para visualização/download
     */
    @Transactional(readOnly = true)
    public String generatePresignedUrl(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with ID: " + attachmentId));

        if (attachment.isDeleted()) {
            throw new BusinessRuleException("Cannot generate URL for deleted attachment");
        }

        return generatePresignedUrlFromKey(attachment.getStorageKey(), attachment.getMimeType());
    }

    /**
     * Lista todos os attachments de um ticket
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponseDTO> getTicketAttachments(UUID ticketId) {
        return attachmentRepository.findByTicketIdAndDeletedAtNull(ticketId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Lista todos os attachments de uma atividade
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponseDTO> getActivityAttachments(UUID activityId) {
        return attachmentRepository.findByActivityIdAndDeletedAtNull(activityId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Lista todos os attachments de um prestador
     */
    @Transactional(readOnly = true)
    public List<AttachmentResponseDTO> getServiceProviderAttachments(UUID serviceProviderId) {
        return attachmentRepository.findByServiceProviderIdAndDeletedAtNull(serviceProviderId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Soft delete de um attachment
     */
    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found with ID: " + attachmentId));

        if (!attachment.isDeleted()) {
            attachment.setDeletedAt(java.time.LocalDateTime.now());
            attachmentRepository.save(attachment);
            log.info("Attachment soft deleted. ID: {}", attachmentId);
        }
    }

    // ============ Private Helper Methods ============

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException(ERROR_FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException(ERROR_FILE_SIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new BusinessRuleException(ERROR_FILE_TYPE);
        }
    }

    private String generateStorageKey(MultipartFile file) {
        LocalDate today = LocalDate.now();
        String datePrefix = String.format("%d/%02d/", today.getYear(), today.getMonthValue());
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String fileName = UUID.randomUUID() + "-" + sanitizeFilename(originalFilename);
        return datePrefix + fileName;
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        int lastDot = filename.lastIndexOf(".");
        return lastDot > 0 ? filename.substring(lastDot + 1).toUpperCase() : "unknown";
    }

    private void uploadToS3(MultipartFile file, String storageKey, String contentType) throws IOException {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType(contentType)
                    .serverSideEncryption(ServerSideEncryption.AES256)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.debug("File uploaded to S3. Key: {}", storageKey);
        } catch (S3Exception e) {
            log.error(ERROR_UPLOADING_S3, e);
            throw new BusinessRuleException(ERROR_UPLOADING_S3 + ": " + e.awsErrorDetails().errorMessage());
        }
    }

    private String generatePresignedUrlFromKey(String storageKey, String mimeType) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .responseContentType(mimeType)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15)) // URL válida por 15 minutos
                    .getObjectRequest(getRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            log.error(ERROR_GENERATING_URL, e);
            throw new BusinessRuleException(ERROR_GENERATING_URL + ": " + e.getMessage());
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
