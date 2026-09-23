package com.lifeos.lifeos.document.service;


import com.lifeos.lifeos.document.entity.*;
import com.lifeos.lifeos.document.repository.*;
import com.lifeos.lifeos.user.entity.User;
import com.lifeos.lifeos.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PdfExtractionService pdfExtractionService;
    private final DocumentContentRepository documentContentRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            StorageService storageService,
            PdfExtractionService pdfExtractionService,
            DocumentContentRepository documentContentRepository
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.pdfExtractionService = pdfExtractionService;
        this.documentContentRepository = documentContentRepository;
    }

    public Document uploadDocument(UUID userId, MultipartFile file) throws IOException {

        // 1. Validate file
        validateFile(file);

        // 2. Find authenticated user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. Store actual file
        String storageKey = storageService.store(file);

        // 4. Create document metadata
        Document document = new Document();
        document.setUser(user);
        document.setFileName(file.getOriginalFilename());
        document.setFileType(file.getContentType());
        document.setFileSize(file.getSize());
        document.setStorageKey(storageKey);
        document.setStatus(DocumentStatus.UPLOADED);

        // 5. Save metadata in PostgreSQL
        return documentRepository.save(document);
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        long maxSize = 10 * 1024 * 1024; // 10 MB

        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must not exceed 10 MB");
        }
    }

    public void extractText(UUID userId, UUID documentId)
            throws IOException {

        Document document = documentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found"
                        )
                );

        // Security check:
        // user can only process their own document
        if (!document.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You do not have access to this document"
            );
        }

        pdfExtractionService.extractText(document);
    }

    public List<DocumentContent> getDocumentText(
            UUID userId,
            UUID documentId
    ) {

        Document document = documentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found"
                        )
                );

        if (!document.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You do not have access to this document"
            );
        }

        return documentContentRepository
                .findByDocumentIdOrderByPageNumberAsc(
                        documentId
                );
    }
}