package com.example.backend.eduSphere.service.impl;

import com.example.backend.common.exceptions.ResourceNotFoundException;
import com.example.backend.eduSphere.dto.request.TemplateRequest;
import com.example.backend.eduSphere.dto.request.UseTemplateRequest;
import com.example.backend.eduSphere.dto.response.TemplateResponse;
import com.example.backend.eduSphere.entity.Template;
import com.example.backend.eduSphere.entity.UserEntity;
import com.example.backend.eduSphere.repository.TemplateRepository;
import com.example.backend.eduSphere.repository.UserRepository;
import com.example.backend.eduSphere.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<TemplateResponse> getAllTemplates() {
        List<Template> templates = templateRepository.findAll();
        return templates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TemplateResponse getTemplateById(String templateId) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id " + templateId));
        return mapToResponse(template);
    }

    @Override
    public TemplateResponse createTemplate(TemplateRequest templateRequest) {
        Template template = new Template();
        template.setName(templateRequest.getName());
        template.setCategory(templateRequest.getCategory());
        template.setSubject(templateRequest.getSubject());
        template.setContent(templateRequest.getContent());
        template.setTargetAudience(templateRequest.getTargetAudience());
        template.setStatus(templateRequest.getStatus());

        // Extract variables from content and save them
        template.setVariables(extractVariablesFromContent(templateRequest.getContent()));

        Template savedTemplate = templateRepository.save(template);
        return mapToResponse(savedTemplate);
    }

    @Override
    public TemplateResponse updateTemplate(String templateId, TemplateRequest templateRequest) {
        Template existingTemplate = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id " + templateId));

        existingTemplate.setName(templateRequest.getName());
        existingTemplate.setCategory(templateRequest.getCategory());
        existingTemplate.setSubject(templateRequest.getSubject());
        existingTemplate.setContent(templateRequest.getContent());
        existingTemplate.setTargetAudience(templateRequest.getTargetAudience());
        existingTemplate.setStatus(templateRequest.getStatus());

        // Update variables from new content
        existingTemplate.setVariables(extractVariablesFromContent(templateRequest.getContent()));

        Template updatedTemplate = templateRepository.save(existingTemplate);
        return mapToResponse(updatedTemplate);
    }

    @Override
    public void deleteTemplate(String templateId) {
        templateRepository.deleteById(templateId);
    }

    @Override
    public void useTemplate(String templateId, UseTemplateRequest useTemplateRequest) {
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id " + templateId));

        // Get the numerical role code from the template
        String roleCode = template.getTargetAudience();

        // Find all users in the target audience based on the role code
        List<UserEntity> targetUsers = userRepository.findByRole(roleCode);

        // For each user, replace variables and send the message
        for (UserEntity user : targetUsers) {
            String personalizedContent = replaceVariables(template.getContent(), useTemplateRequest.getVariableValues(), user);

            // This is where you would send the message.
            // For now, we'll just print it to the console.
            System.out.println("--- Sending Personalized Message ---");
            System.out.println("To: " + user.getName() + " (" + user.getEmail() + ")");
            System.out.println("Subject: " + replaceVariables(template.getSubject(), useTemplateRequest.getVariableValues(), user));
            System.out.println("Content:\n" + personalizedContent);
            System.out.println("------------------------------------");
        }
    }

    // Helper method to extract variables from content, e.g., "Hi {name}" -> "name"
    private List<String> extractVariablesFromContent(String content) {
        if (content == null) {
            return List.of();
        }
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = pattern.matcher(content);
        return matcher.results()
                .map(matchResult -> matchResult.group(1).trim())
                .distinct()
                .collect(Collectors.toList());
    }

    // Helper method to replace variables in content, e.g., "Hi {name}" -> "Hi John"
    private String replaceVariables(String content, List<UseTemplateRequest.VariableValue> variableValues, UserEntity user) {
        String result = content;
        for (UseTemplateRequest.VariableValue var : variableValues) {
            String placeholder = "{" + var.getName() + "}";
            String value = var.getValue();
            result = result.replace(placeholder, value);
        }

        // Also replace special user-related variables
        result = result.replace("{name}", user.getName());
        result = result.replace("{email}", user.getEmail());

        return result;
    }

    private TemplateResponse mapToResponse(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getName(),
                template.getCategory(),
                template.getSubject(),
                template.getContent(),
                template.getVariables(),
                template.getTargetAudience(),
                template.getStatus(),
                template.getCreatedAt(),
                template.getUpdatedAt()
        );
    }
}