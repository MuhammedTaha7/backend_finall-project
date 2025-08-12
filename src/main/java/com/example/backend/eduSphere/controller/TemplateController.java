package com.example.backend.eduSphere.controller;

import com.example.backend.eduSphere.dto.request.TemplateRequest;
import com.example.backend.eduSphere.dto.request.UseTemplateRequest;
import com.example.backend.eduSphere.dto.response.TemplateResponse;
import com.example.backend.eduSphere.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        List<TemplateResponse> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable String id) {
        TemplateResponse template = templateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(@RequestBody TemplateRequest request) {
        TemplateResponse createdTemplate = templateService.createTemplate(request);
        return ResponseEntity.ok(createdTemplate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(@PathVariable String id, @RequestBody TemplateRequest request) {
        TemplateResponse updatedTemplate = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<Void> useTemplate(@PathVariable String id, @RequestBody UseTemplateRequest request) {
        templateService.useTemplate(id, request);
        return ResponseEntity.ok().build();
    }
}