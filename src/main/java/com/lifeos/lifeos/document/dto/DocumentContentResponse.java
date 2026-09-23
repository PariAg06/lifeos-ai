package com.lifeos.lifeos.document.dto;

import com.lifeos.lifeos.document.entity.DocumentContent;

import java.util.UUID;

public record DocumentContentResponse(
        UUID id,
        UUID documentId,
        Integer pageNumber,
        String content
) {

    public static DocumentContentResponse from(
            DocumentContent content
    ) {

        return new DocumentContentResponse(
                content.getId(),
                content.getDocument().getId(),
                content.getPageNumber(),
                content.getContent()
        );
    }
}
