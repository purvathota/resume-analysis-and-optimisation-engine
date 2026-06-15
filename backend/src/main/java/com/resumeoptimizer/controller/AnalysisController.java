package com.resumeoptimizer.controller;

import com.resumeoptimizer.entity.Analysis;
import com.resumeoptimizer.exception.AiServiceException;
import com.resumeoptimizer.service.analysis.AnalysisService;
import com.resumeoptimizer.service.document.DocumentExportService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final AnalysisService analysisService;
    private final DocumentExportService documentExportService;
    private final com.resumeoptimizer.repository.AnalysisRepository analysisRepository;

    @PostMapping("/{resumeId}/ats")
    public ResponseEntity<?> performAtsAnalysis(
            @PathVariable Long resumeId,
            @RequestParam Long jobDescriptionId) {
        try {
            log.info("Starting ATS analysis for resumeId={}, jobDescriptionId={}", resumeId, jobDescriptionId);
            Analysis analysis = analysisService.performAtsAnalysis(resumeId, jobDescriptionId);
            log.info("ATS analysis completed successfully. ATS Score: {}", analysis.getAtsScore());
            return ResponseEntity.ok(analysis);
        } catch (AiServiceException e) {
            log.error("AI service error during ATS analysis: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
            problem.setTitle("AI Service Error");
            problem.setProperty("timestamp", Instant.now());
            problem.setProperty("resumeId", resumeId);
            problem.setProperty("jobDescriptionId", jobDescriptionId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
        } catch (RuntimeException e) {
            log.error("Error during ATS analysis: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            problem.setTitle("Analysis Error");
            problem.setProperty("timestamp", Instant.now());
            return ResponseEntity.badRequest().body(problem);
        } catch (Exception e) {
            log.error("Unexpected error during ATS analysis: {}", e.getMessage(), e);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during analysis. Please try again.");
            problem.setTitle("Internal Server Error");
            problem.setProperty("timestamp", Instant.now());
            return ResponseEntity.internalServerError().body(problem);
        }
    }

    @PostMapping("/{resumeId}/recruiter")
    public ResponseEntity<?> performRecruiterReview(
            @PathVariable Long resumeId,
            @RequestParam Long jobDescriptionId) {
        try {
            log.info("Starting Recruiter Review for resumeId={}, jobDescriptionId={}", resumeId, jobDescriptionId);
            Analysis analysis = analysisService.performRecruiterReview(resumeId, jobDescriptionId);
            log.info("Recruiter Review completed successfully. Fit Score: {}", analysis.getRecruiterFitScore());
            return ResponseEntity.ok(analysis);
        } catch (AiServiceException e) {
            log.error("AI service error during Recruiter Review: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
            problem.setTitle("AI Service Error");
            problem.setProperty("timestamp", Instant.now());
            problem.setProperty("resumeId", resumeId);
            problem.setProperty("jobDescriptionId", jobDescriptionId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
        } catch (RuntimeException e) {
            log.error("Error during Recruiter Review: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            problem.setTitle("Analysis Error");
            problem.setProperty("timestamp", Instant.now());
            return ResponseEntity.badRequest().body(problem);
        } catch (Exception e) {
            log.error("Unexpected error during Recruiter Review: {}", e.getMessage(), e);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during analysis. Please try again.");
            problem.setTitle("Internal Server Error");
            problem.setProperty("timestamp", Instant.now());
            return ResponseEntity.internalServerError().body(problem);
        }
    }

    @PostMapping("/{resumeId}/optimize")
    public ResponseEntity<?> performOptimization(
            @PathVariable Long resumeId,
            @RequestParam Long jobDescriptionId) {
        try {
            log.info("Starting Resume Optimization for resumeId={}, jobDescriptionId={}", resumeId, jobDescriptionId);
            Analysis analysis = analysisService.performResumeOptimization(resumeId, jobDescriptionId);
            log.info("Resume Optimization completed successfully.");
            return ResponseEntity.ok(analysis);
        } catch (AiServiceException e) {
            log.error("AI service error during Resume Optimization: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
            problem.setTitle("AI Service Error");
            problem.setProperty("timestamp", Instant.now());
            problem.setProperty("resumeId", resumeId);
            problem.setProperty("jobDescriptionId", jobDescriptionId);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
        } catch (RuntimeException e) {
            log.error("Error during Resume Optimization: {}", e.getMessage());
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
            problem.setTitle("Analysis Error");
            problem.setProperty("timestamp", Instant.now());
            return ResponseEntity.badRequest().body(problem);
        } catch (Exception e) {
            log.error("Unexpected error during Resume Optimization: {}", e.getMessage(), e);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during optimization. Please try again.");
            problem.setTitle("Internal Server Error");
            problem.setProperty("timestamp", Instant.now());
            return ResponseEntity.internalServerError().body(problem);
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/{resumeId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable Long resumeId,
            @RequestParam Long jobDescriptionId) {
        try {
            Analysis analysis = analysisRepository.findByResumeIdAndJobDescriptionId(resumeId, jobDescriptionId)
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));
            
            byte[] pdfBytes = documentExportService.generatePdf(analysis);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Optimized_Resume.pdf");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping("/{resumeId}/export/docx")
    public ResponseEntity<byte[]> exportDocx(
            @PathVariable Long resumeId,
            @RequestParam Long jobDescriptionId) {
        try {
            Analysis analysis = analysisRepository.findByResumeIdAndJobDescriptionId(resumeId, jobDescriptionId)
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));
            
            byte[] docxBytes = documentExportService.generateDocx(analysis);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            headers.setContentDispositionFormData("attachment", "Optimized_Resume.docx");
            
            return new ResponseEntity<>(docxBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error generating DOCX", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
