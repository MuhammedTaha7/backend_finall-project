package com.example.backend.service.impl;

import com.example.backend.dto.request.GenerateReportRequest;
import com.example.backend.dto.response.GenerateReportResponse;
import com.example.backend.service.OpenAiService;
import com.example.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final MongoTemplate mongoTemplate;
    private final OpenAiService openAiService;

    @Override
    public GenerateReportResponse generateReport(GenerateReportRequest request) {

        System.out.println("➡️ Received request from frontend: " + request.getQuery());

        String userQuery = request.getQuery();

        // 🧠 Step 1: Get MongoDB-like query string from GPT
        String mongoQueryString = openAiService.generateMongoQuery(userQuery);
        System.out.println("Generated MongoDB Query from GPT:\n" + mongoQueryString);

        try {
            Document responseDoc = Document.parse(mongoQueryString.trim());

            Document filter = responseDoc.containsKey("filter")
                    ? (Document) responseDoc.get("filter")
                    : responseDoc;

            BasicQuery query = new BasicQuery(filter);

            if (responseDoc.containsKey("sort")) {
                Document sortDoc = (Document) responseDoc.get("sort");
                sortDoc.forEach((field, direction) -> {
                    int dir = (int) direction;
                    query.with(org.springframework.data.domain.Sort.by(
                            dir == -1 ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC,
                            field
                    ));
                });
            }

            if (responseDoc.containsKey("limit")) {
                int limit = responseDoc.getInteger("limit", 0);
                if (limit > 0) {
                    query.limit(limit);
                }
            }

            List<Map> results = mongoTemplate.find(query, Map.class, "users");

            return GenerateReportResponse.builder()
                    .data((List<Map<String, Object>>)(List<?>) results)
                    .build();

        } catch (Exception e) {
            System.out.println("❌ Failed to parse GPT response: " + e.getMessage());
            return GenerateReportResponse.builder().data(List.of()).build();
        }
    }
}