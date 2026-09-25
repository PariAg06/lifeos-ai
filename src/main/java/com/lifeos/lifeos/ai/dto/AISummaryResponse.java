package com.lifeos.lifeos.ai.dto;

import java.util.List;
import java.util.UUID;

public record AISummaryResponse(
        UUID documentId,
        String summary,
        List<String> keyPoints
) {
}