package com.lifeos.lifeos.ai.controller;

import com.lifeos.lifeos.ai.dto.AIQuestionRequest;
import com.lifeos.lifeos.ai.dto.AIQuestionResponse;
import com.lifeos.lifeos.ai.service.AIQuestionService;
import com.lifeos.lifeos.user.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class AIQuestionController {

    private final AIQuestionService aiQuestionService;

    public AIQuestionController(
            AIQuestionService aiQuestionService) {

        this.aiQuestionService = aiQuestionService;
    }

    @PostMapping("/{documentId}/ask")
    public ResponseEntity<AIQuestionResponse> ask(
            @PathVariable UUID documentId,
            @RequestBody AIQuestionRequest request,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        AIQuestionResponse response =
                aiQuestionService.ask(
                        user.getId(),
                        documentId,
                        request.question()
                );

        return ResponseEntity.ok(response);
    }
}