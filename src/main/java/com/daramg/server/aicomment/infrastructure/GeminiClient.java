package com.daramg.server.aicomment.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClient {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";

    private final RestClient restClient;
    private final String apiKey;

    public GeminiClient(@Value("${gemini.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.create();
    }

    public String generateComment(String systemInstruction, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                        "parts", List.of(Map.of("text", systemInstruction))
                ),
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", userPrompt)))
                ),
                "generationConfig", Map.of(
                        "maxOutputTokens", 100
                )
        );

        try {
            Map<?, ?> response = restClient.post()
                    .uri(GEMINI_URL + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return extractText(response);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("Gemini API HTTP 오류 - status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API 호출 실패", e);
        } catch (Exception e) {
            log.error("Gemini API 호출 실패 - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Gemini API 호출 실패", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> response) {
        List<?> candidates = (List<?>) response.get("candidates");
        Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
        Map<?, ?> content = (Map<?, ?>) candidate.get("content");
        List<?> parts = (List<?>) content.get("parts");
        Map<?, ?> part = (Map<?, ?>) parts.get(0);
        return (String) part.get("text");
    }
}
