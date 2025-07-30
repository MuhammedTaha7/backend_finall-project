package com.example.backend.community.controller;

import com.example.backend.community.service.JobsService;
import com.example.backend.community.dto.JobDto;
import com.example.backend.community.dto.JobApplicationDto;
import com.example.backend.community.dto.request.CreateJobRequest;
import com.example.backend.community.dto.request.UpdateJobRequest;
import com.example.backend.community.dto.request.ApplyToJobRequest;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class JobsController {

    @Autowired
    private JobsService jobsService;

    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search) {
        List<JobDto> jobs = jobsService.getAllJobs(type, location, search);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/my-posts")
    public ResponseEntity<List<JobDto>> getMyPostedJobs(Authentication authentication) {
        String userId = authentication.getName();
        List<JobDto> jobs = jobsService.getJobsByPoster(userId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/applied")
    public ResponseEntity<List<JobDto>> getAppliedJobs(Authentication authentication) {
        String userId = authentication.getName();
        List<JobDto> jobs = jobsService.getAppliedJobs(userId);
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/saved")
    public ResponseEntity<List<JobDto>> getSavedJobs(Authentication authentication) {
        String userId = authentication.getName();
        List<JobDto> jobs = jobsService.getSavedJobs(userId);
        return ResponseEntity.ok(jobs);
    }

    @PostMapping
    public ResponseEntity<JobDto> createJob(@RequestBody CreateJobRequest request, Authentication authentication) {
        String userId = authentication.getName();
        JobDto job = jobsService.createJob(request, userId);
        return ResponseEntity.ok(job);
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<JobDto> updateJob(
            @PathVariable String jobId,
            @RequestBody UpdateJobRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        JobDto job = jobsService.updateJob(jobId, request, userId);
        return ResponseEntity.ok(job);
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable String jobId, Authentication authentication) {
        String userId = authentication.getName();
        jobsService.deleteJob(jobId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{jobId}/apply")
    public ResponseEntity<Void> applyToJob(
            @PathVariable String jobId,
            @RequestBody ApplyToJobRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        jobsService.applyToJob(jobId, request, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{jobId}/save")
    public ResponseEntity<Void> saveJob(@PathVariable String jobId, Authentication authentication) {
        String userId = authentication.getName();
        jobsService.saveJob(jobId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{jobId}/save")
    public ResponseEntity<Void> unsaveJob(@PathVariable String jobId, Authentication authentication) {
        String userId = authentication.getName();
        jobsService.unsaveJob(jobId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{jobId}/applications")
    public ResponseEntity<List<JobApplicationDto>> getJobApplications(
            @PathVariable String jobId,
            Authentication authentication) {
        String userId = authentication.getName();

        // Use the enhanced method that includes CV data
        List<JobApplicationDto> applications = jobsService.getJobApplicationsWithCVData(jobId, userId);
        return ResponseEntity.ok(applications);
    }

    @PutMapping("/applications/{applicationId}/accept")
    public ResponseEntity<Void> acceptApplication(
            @PathVariable String applicationId,
            Authentication authentication) {
        String userId = authentication.getName();
        jobsService.acceptApplication(applicationId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/applications/{applicationId}/reject")
    public ResponseEntity<Void> rejectApplication(
            @PathVariable String applicationId,
            Authentication authentication) {
        String userId = authentication.getName();
        jobsService.rejectApplication(applicationId, userId);
        return ResponseEntity.ok().build();
    }

    // This matches your frontend call: /api/jobs/cv/download/{applicantId}
    @GetMapping("/cv/download/{applicantId}")
    public ResponseEntity<Resource> downloadApplicantCV(
            @PathVariable String applicantId,
            Authentication authentication) {
        String userId = authentication.getName();

        try {
            Resource resource = jobsService.downloadApplicantCV(applicantId, userId);

            // Create a meaningful filename
            String filename = String.format("cv_%s.pdf", applicantId);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/pdf")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}