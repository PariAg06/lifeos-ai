package com.lifeos.lifeos.ai.controller;

import com.lifeos.lifeos.ai.dto.AISummaryResponse;
import com.lifeos.lifeos.ai.service.AISummaryService;
import com.lifeos.lifeos.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class AISummaryController {

    private final AISummaryService aiSummaryService;

    public AISummaryController(
            AISummaryService aiSummaryService
    ) {
        this.aiSummaryService = aiSummaryService;
    }

    @PostMapping("/{documentId}/summarize")
    public ResponseEntity<AISummaryResponse> summarize(
            @PathVariable UUID documentId,
            Authentication authentication
    ) {

        User user = (User) authentication.getPrincipal();

        AISummaryResponse response =
                aiSummaryService.summarize(
                        user.getId(),
                        documentId
                );

        return ResponseEntity.ok(response);
    }
}