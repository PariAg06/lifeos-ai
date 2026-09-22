package com.lifeos.lifeos.document.dto;

import com.lifeos.lifeos.document.entity.Document;
import com.lifeos.lifeos.document.entity.DocumentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID documentId,
        String fileName,
        String fileType,
        Long fileSize,
        DocumentStatus status,
        LocalDateTime createdAt
) {

    public static DocumentResponse from(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getFileName(),
                document.getFileType(),
                document.getFileSize(),
                document.getStatus(),
                document.getCreatedAt()
        );
    }
}
