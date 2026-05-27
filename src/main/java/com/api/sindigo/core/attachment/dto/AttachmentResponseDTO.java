package com.api.sindigo.core.attachment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponseDTO {
    private UUID id;
    private String originalFileName;
    private String fileType;
    private String mimeType;
    private Long sizeBytes;
    private String attachmentCategory;
    private UUID uploadedBy;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
