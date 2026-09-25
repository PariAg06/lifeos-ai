package com.lifeos.lifeos.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OllamaAIProvider extends AIProvider {

    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String model;

    public OllamaAIProvider(
            ObjectMapper objectMapper,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model) {

        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @Override
    public String generate(String prompt) {

        try {
            String requestBody = objectMapper.writeValueAsString(
                    new OllamaRequest(model, prompt, false)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Ollama returned HTTP " + response.statusCode()
                                + ": " + response.body()
                );
            }

            JsonNode json = objectMapper.readTree(response.body());

            return json.get("response").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to communicate with Ollama", e);
        }
    }

    private record OllamaRequest(
            String model,
            String prompt,
            boolean stream
    ) {
    }
}