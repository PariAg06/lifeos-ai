package com.lifeos.lifeos.document.repository;

import com.lifeos.lifeos.document.entity.DocumentContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentContentRepository
        extends JpaRepository<DocumentContent, UUID> {

    List<DocumentContent> findByDocumentIdOrderByPageNumberAsc(
            UUID documentId
    );

    void deleteByDocumentId(UUID documentId);


}