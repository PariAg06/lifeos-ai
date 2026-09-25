package com.lifeos.lifeos.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeos.lifeos.ai.dto.AISummaryResponse;
import com.lifeos.lifeos.document.entity.Document;
import com.lifeos.lifeos.document.entity.DocumentContent;
import com.lifeos.lifeos.document.repository.DocumentContentRepository;
import com.lifeos.lifeos.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AISummaryService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final ObjectMapper objectMapper;
    private final AIProvider aiProvider;

    public AISummaryService(
            DocumentRepository documentRepository,
            DocumentContentRepository documentContentRepository,
            ObjectMapper objectMapper,
            AIProvider aiProvider
    ) {
        this.documentRepository = documentRepository;
        this.documentContentRepository = documentContentRepository;
        this.objectMapper = objectMapper;
        this.aiProvider = aiProvider;
    }

    public AISummaryResponse summarize(
            UUID userId,
            UUID documentId
    ) {

        // 1. Find document
        Document document = documentRepository
                .findById(documentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found"
                        )
                );

        // 2. Security check
        if (!document.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You do not have access to this document"
            );
        }

        // 3. Retrieve extracted text
        List<DocumentContent> contents =
                documentContentRepository
                        .findByDocumentIdOrderByPageNumberAsc(
                                documentId
                        );

        if (contents.isEmpty()) {
            throw new IllegalArgumentException(
                    "Document text has not been extracted yet"
            );
        }

        // 4. Combine page text
        String documentText = contents.stream()
                .map(DocumentContent::getContent)
                .filter(content ->
                        content != null && !content.isBlank()
                )
                .collect(Collectors.joining("\n\n"));

        if (documentText.isBlank()) {
            throw new IllegalArgumentException(
                    "Document contains no readable text"
            );
        }

        // 5. Build prompt
        String prompt = """
                You are a document summarization assistant.

                Summarize the document provided below.

                IMPORTANT:
                - Use ONLY information present in the document.
                - Do not invent facts.
                - Do not assume information that is not present.
                - Keep the summary concise but useful.
                - Extract the most important facts as key points.
                - If something is unclear or missing, do not make it up.

                Return JSON in exactly this structure:

                {
                  "summary": "A concise summary of the document",
                  "keyPoints": [
                    "Important point 1",
                    "Important point 2",
                    "Important point 3"
                  ]
                }

                DOCUMENT:
                --------------------
                %s
                --------------------
                """.formatted(documentText);

        // 6. Call local Ollama model
        String aiOutput = aiProvider.generate(prompt);

        // 7. Check model response
        if (aiOutput == null || aiOutput.isBlank()) {
            throw new IllegalStateException(
                    "AI returned an empty response"
            );
        }

        // 8. Parse JSON returned by AI
        try {

            AIResponse aiResponse =
                    objectMapper.readValue(
                            aiOutput,
                            AIResponse.class
                    );

            return new AISummaryResponse(
                    documentId,
                    aiResponse.summary(),
                    aiResponse.keyPoints()
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Failed to parse AI response: " + aiOutput,
                    e
            );
        }
    }

    private record AIResponse(
            String summary,
            List<String> keyPoints
    ) {
    }
}