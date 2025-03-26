package com.example.backend.controller;

import com.example.backend.dto.request.GenerateReportRequest;
import com.example.backend.dto.response.GenerateReportResponse;
import com.example.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate-report")
    public ResponseEntity<GenerateReportResponse> generateReport(@RequestBody GenerateReportRequest request) {
        GenerateReportResponse response = reportService.generateReport(request);
        return ResponseEntity.ok(response);
    }
}
