package com.resumeoptimizer.controller;

import com.resumeoptimizer.dto.request.JobApplicationRequest;
import com.resumeoptimizer.dto.response.JobApplicationResponse;
import com.resumeoptimizer.entity.ApplicationStatus;
import com.resumeoptimizer.security.CustomUserDetails;
import com.resumeoptimizer.service.jobtracker.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job-applications")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getUserApplications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(jobApplicationService.getUserApplications(userDetails.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> createApplication(
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        JobApplicationResponse response = jobApplicationService.createApplication(userDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(jobApplicationService.updateApplication(id, userDetails.getUser(), request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ApplicationStatus status = ApplicationStatus.valueOf(payload.get("status"));
        return ResponseEntity.ok(jobApplicationService.updateStatus(id, userDetails.getUser(), status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        jobApplicationService.deleteApplication(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }
}
