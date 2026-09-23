package com.lifeos.lifeos.document.service;

import com.lifeos.lifeos.document.entity.Document;
import com.lifeos.lifeos.document.entity.DocumentContent;
import com.lifeos.lifeos.document.repository.DocumentContentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractionService {

    private final StorageService storageService;
    private final DocumentContentRepository documentContentRepository;

    public PdfExtractionService(
            StorageService storageService,
            DocumentContentRepository documentContentRepository
    ) {
        this.storageService = storageService;
        this.documentContentRepository = documentContentRepository;
    }

    @Transactional
    public List<DocumentContent> extractText(Document document)
            throws IOException {

        // Remove old extracted content if extraction is repeated
        documentContentRepository.deleteByDocumentId(
                document.getId()
        );

        Path pdfPath = storageService.load(
                document.getStorageKey()
        );

        List<DocumentContent> contents = new ArrayList<>();

        try (PDDocument pdfDocument = Loader.loadPDF(
                pdfPath.toFile()
        )) {

            PDFTextStripper stripper = new PDFTextStripper();

            int totalPages = pdfDocument.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {

                stripper.setStartPage(page);
                stripper.setEndPage(page);

                String text = stripper.getText(pdfDocument);

                if (text != null && !text.isBlank()) {

                    // PostgreSQL TEXT cannot store NUL (\u0000) characters
                    String cleanedText = text.replace("\u0000", "");

                    if (!cleanedText.isBlank()) {

                        DocumentContent content = new DocumentContent();

                        content.setDocument(document);
                        content.setPageNumber(page);
                        content.setContent(cleanedText.trim());

                        contents.add(content);
                    }
                }
            }
        }

        return documentContentRepository.saveAll(contents);
    }
}
