package com.example.backend.service;

public interface OpenAiService {
    String generateMongoQuery(String naturalLanguagePrompt);
}
