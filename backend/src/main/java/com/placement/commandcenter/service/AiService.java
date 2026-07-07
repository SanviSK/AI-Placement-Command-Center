package com.placement.commandcenter.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateContent(String prompt) {
        String apiKey = geminiApiKey;
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("placeholder")) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("GEMINI_API_KEY is not configured in environment or properties.");
            return null;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Construct Gemini Request Format
            Map<String, Object> requestBody = new HashMap<>();
            
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> contentObj = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> partObj = new HashMap<>();
            
            partObj.put("text", prompt);
            parts.add(partObj);
            contentObj.put("parts", parts);
            contents.add(contentObj);
            requestBody.put("contents", contents);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode textNode = root.path("candidates").get(0)
                        .path("content").path("parts").get(0).path("text");
                
                String text = textNode.asText();
                return cleanJsonString(text);
            }
        } catch (Exception e) {
            System.err.println("Error calling Gemini API: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private String cleanJsonString(String input) {
        String cleaned = input.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
