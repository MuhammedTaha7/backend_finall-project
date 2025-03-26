package com.example.backend.service.impl;

import com.example.backend.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAiServiceImpl implements OpenAiService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

//    @Override
//    public String generateMongoQuery(String naturalLanguagePrompt) {
//        System.out.println("🧠 [MOCK GPT] Received prompt: " + naturalLanguagePrompt);
//
//        // رد وهمي لاستعلام MongoDB
//        String fakeQuery = """
//    {
//      "role": "student",
//      "gpa": { "$gte": 3.5 }
//    }
//    """;
//
//        return fakeQuery;
//    }

    @Override
    public String generateMongoQuery(String naturalLanguagePrompt) {
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
            - Collection name is 'users'
            - Available fields: name (string), email (string), role (student | lecturer), gpa (number), semester (number), feesPaid (boolean)
            - Field names are case-sensitive → use lowercase as shown above.
            - If query asks for top/bottom X, include sort and limit.
            - If query is general like "all users", use filter: {}
            
            Examples:
            
            Input: top 5 students by gpa  
            Output:
            {
              "filter": { "role": "student" },
              "sort": { "gpa": -1 },
              "limit": 5
            }
            
            Input: all students in semester 2  
            Output:
            {
              "filter": { "role": "student", "semester": 2 }
            }
            
            Input: lecturers only  
            Output:
            {
              "filter": { "role": "lecturer" }
            }
            
            Input: students with unpaid fees  
            Output:
            {
              "filter": { "role": "student", "feesPaid": false }
            }
            """;




        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", naturalLanguagePrompt)
        ));
        requestBody.put("temperature", 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(openaiApiUrl, request, Map.class);

        System.out.println("GPT FULL RESPONSE: " + response.getBody());

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
