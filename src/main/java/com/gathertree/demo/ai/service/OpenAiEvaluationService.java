package com.gathertree.demo.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gathertree.demo.ai.dto.TreeEvaluationResponse;
import com.gathertree.demo.global.response.exception.GeneralException;
import com.gathertree.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiEvaluationService implements OpenAiEvaluationFacade {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENAI_URL = "https://api.openai.com/v1/responses";

    @Override
    public TreeEvaluationResponse evaluate(String imageUrl, String mode) {

        try {
            String prompt = AiPromptFactory.buildPrompt(mode);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "input", new Object[]{
                            Map.of(
                                    "role", "user",
                                    "content", new Object[]{
                                            Map.of("type", "input_text", "text", prompt),
                                            Map.of("type", "input_image", "image_url", imageUrl)
                                    }
                            )
                    }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    OPENAI_URL,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String text = root.path("output").get(0).path("content").get(0).path("text").asText();
            JsonNode json = objectMapper.readTree(text);

            return TreeEvaluationResponse.builder()
                    .score(json.get("score").asInt())
                    .title(json.get("title").asText())
                    .summary(json.get("summary").asText())
                    .comments(
                            objectMapper.convertValue(json.get("comments"), List.class)
                    )
                    .build();

        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.AI_API_ERROR, e);
        }
    }
}
