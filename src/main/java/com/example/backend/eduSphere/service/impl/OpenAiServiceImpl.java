package com.example.backend.eduSphere.service.impl;

import com.example.backend.eduSphere.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    // Inject API key and URL from application.properties or application.yml
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate(); // Used to send HTTP requests to REST APIs

    @Override
    public String generateMongoQuery(String naturalLanguagePrompt) {
        // The system prompt gives instructions to GPT on how to behave and format the output
        String systemPrompt = """
            You are a MongoDB expert.
            Your task is to convert natural language into structured JSON queries for MongoDB.
            
            Use this structure always:
            {
              "filter": { ... },
              "sort": { "fieldName": 1 or -1 },
              "limit": number
            }
            
            Rules:
            - Only return JSON, no explanation or extra text.
            - Collection: users.
            - Fields: username, email, role (1300=student | 1200=lecturer | 1100=admin), profilePicture.
            - Do not use or expose the password field.
            - Field names are case-sensitive → use lowercase as shown above.
            - If query asks for top/bottom X, include sort and limit.
            - If query is general like "all users", use filter: {}
            - For queries like "names that start with X", use regex: { "username": { "$regex": "^X", "$options": "i" } }
            
            Examples:
            
            Input: top 5 students by gpa  
            Output:
            {
              "filter": { "role": "1300" },
              "sort": { "gpa": -1 },
              "limit": 5
            }
            
            Input: all admins  
            Output:
            {
              "filter": { "role": "1100" }
            }
            
            Input: users that start with the letter m  
            Output:
            {
              "filter": { "username": { "$regex": "^m", "$options": "i" } }
            }
            """;

        // Build the request body for OpenAI Chat API
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        // Provide both system (instructions) and user (natural language input) messages
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", naturalLanguagePrompt)
        ));

        // Lower temperature means more predictable and focused results
        requestBody.put("temperature", 0);

        // Set headers: JSON content type and Bearer token for authorization
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        // Wrap headers and body in HttpEntity to send the POST request
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // Send the request and receive a typed response as a Map
        ResponseEntity<Map> response = restTemplate.postForEntity(openaiApiUrl, request, Map.class);

        // Extract the "choices" list from the response
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

        // Get the actual message from the first choice
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        // Return the "content" part which contains the generated MongoDB query
        return (String) message.get("content");
    }
}