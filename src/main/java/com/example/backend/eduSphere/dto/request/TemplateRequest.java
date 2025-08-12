package com.example.backend.eduSphere.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {

    private List<String> recipientIds;

    private String name;
    private String category;
    private String subject;
    private String content;
    private List<String> variables;
    private String targetAudience;
    private String status;
}