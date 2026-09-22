package com.lifeos.lifeos.document.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class StorageService {

    private final Path rootLocation = Paths.get("uploads");

    public StorageService() throws IOException {
        Files.createDirectories(rootLocation);
    }

    public String store(MultipartFile file) throws IOException {

        String fileId = UUID.randomUUID().toString();

        String originalFileName = file.getOriginalFilename();

        String storageKey = "documents/" + fileId + "/" + originalFileName;

        Path destination = rootLocation.resolve(storageKey);

        Files.createDirectories(destination.getParent());

        file.transferTo(destination);

        return storageKey;
    }
}

