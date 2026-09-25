package com.lifeos.lifeos.ai.service;

import com.lifeos.lifeos.ai.dto.AIQuestionResponse;
import com.lifeos.lifeos.document.entity.Document;
import com.lifeos.lifeos.document.entity.DocumentContent;
import com.lifeos.lifeos.document.repository.DocumentContentRepository;
import com.lifeos.lifeos.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AIQuestionService {

    private final DocumentRepository documentRepository;
    private final DocumentContentRepository documentContentRepository;
    private final AIProvider aiProvider;

    public AIQuestionService(
            DocumentRepository documentRepository,
            DocumentContentRepository documentContentRepository,
            AIProvider aiProvider) {

        this.documentRepository = documentRepository;
        this.documentContentRepository = documentContentRepository;
        this.aiProvider = aiProvider;
    }

    public AIQuestionResponse ask(
            UUID userId,
            UUID documentId,
            String question) {

        // 1. Validate question
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question cannot be empty");
        }

        // 2. Find document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Document not found"));

        // 3. Verify document belongs to current user
        if (!document.getUser().getId().equals(userId)) {
            throw new RuntimeException("You do not have access to this document");
        }

        // 4. Retrieve extracted document pages
        List<DocumentContent> contents =
                documentContentRepository
                        .findByDocumentIdOrderByPageNumberAsc(documentId);

        if (contents.isEmpty()) {
            throw new RuntimeException(
                    "No extracted text found for this document");
        }

        // 5. Build document context
        StringBuilder context = new StringBuilder();

        for (DocumentContent content : contents) {
            context.append("\n--- Page ")
                    .append(content.getPageNumber())
                    .append(" ---\n");

            context.append(content.getContent())
                    .append("\n");
        }

        // 6. Build grounded prompt
        String prompt = """
                You are answering a question about an uploaded document.

                Answer ONLY using information contained in the document.

                If the answer cannot be found in the document,
                say: "The answer is not available in the document."

                Do not use outside knowledge.
                Do not invent information.

                Question:
                %s

                Document:
                %s

                Answer the question clearly and concisely.
                """.formatted(
                question,
                context
        );

        // 7. Ask the AI
        String answer = aiProvider.generate(prompt);

        if (answer == null || answer.isBlank()) {
            throw new RuntimeException("AI returned an empty answer");
        }

        // 8. Determine source page
        String source = findSourcePage(answer, contents);

        return new AIQuestionResponse(
                answer.trim(),
                source
        );
    }

    private String findSourcePage(
            String answer,
            List<DocumentContent> contents) {

        String normalizedAnswer = answer.toLowerCase();

        for (DocumentContent content : contents) {

            String pageText = content.getContent().toLowerCase();

            String[] words = normalizedAnswer.split("\\s+");

            int matchingWords = 0;

            for (String word : words) {
                if (word.length() > 4 && pageText.contains(word)) {
                    matchingWords++;
                }
            }

            if (matchingWords >= 2) {
                return "Page " + content.getPageNumber();
            }
        }

        return "Document";
    }
}
