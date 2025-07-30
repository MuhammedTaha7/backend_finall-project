package com.example.backend.eduSphere.service;

import com.example.backend.eduSphere.dto.request.GenerateReportRequest;
import com.example.backend.eduSphere.dto.response.GenerateReportResponse;

public interface ReportService {
    GenerateReportResponse generateReport(GenerateReportRequest request);
}
