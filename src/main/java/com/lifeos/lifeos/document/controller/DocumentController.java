package com.lifeos.lifeos.document.controller;

import com.lifeos.lifeos.document.dto.*;
import com.lifeos.lifeos.document.entity.*;
import com.lifeos.lifeos.document.service.DocumentService;
import com.lifeos.lifeos.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        User user = (User) authentication.getPrincipal();

        UUID userId = user.getId();

        Document document =
                documentService.uploadDocument(userId, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DocumentResponse.from(document));
    }

    @PostMapping("/{documentId}/extract")
    public ResponseEntity<String> extractText(
            @PathVariable UUID documentId,
            Authentication authentication
    ) throws IOException {

        User user = (User) authentication.getPrincipal();

        documentService.extractText(
                user.getId(),
                documentId
        );

        return ResponseEntity.ok(
                "Document text extracted successfully"
        );
    }

    @GetMapping("/{documentId}/text")
    public ResponseEntity<List<DocumentContentResponse>> getDocumentText(
            @PathVariable UUID documentId,
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();

        List<DocumentContent> contents =
                documentService.getDocumentText(
                        user.getId(),
                        documentId
                );

        List<DocumentContentResponse> response =
                contents.stream()
                        .map(DocumentContentResponse::from)
                        .toList();

        return ResponseEntity.ok(response);
    }
}