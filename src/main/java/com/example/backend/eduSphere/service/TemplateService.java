package com.example.backend.eduSphere.service;

import com.example.backend.eduSphere.dto.request.TemplateRequest;
import com.example.backend.eduSphere.dto.request.UseTemplateRequest;
import com.example.backend.eduSphere.dto.response.TemplateResponse;

import java.util.List;

public interface TemplateService {

    List<TemplateResponse> getAllTemplates();

    TemplateResponse getTemplateById(String templateId);

    TemplateResponse createTemplate(TemplateRequest templateRequest);

    TemplateResponse updateTemplate(String templateId, TemplateRequest templateRequest);

    void deleteTemplate(String templateId);

    void useTemplate(String templateId, UseTemplateRequest useTemplateRequest);
}