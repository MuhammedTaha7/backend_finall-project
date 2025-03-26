package com.example.backend.service;

import com.example.backend.dto.request.GenerateReportRequest;
import com.example.backend.dto.response.GenerateReportResponse;

public interface ReportService {
    GenerateReportResponse generateReport(GenerateReportRequest request);
}
