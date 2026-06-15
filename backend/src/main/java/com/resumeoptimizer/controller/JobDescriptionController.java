package com.resumeoptimizer.controller;

import com.resumeoptimizer.dto.response.UploadResponse;
import com.resumeoptimizer.entity.JobDescription;
import com.resumeoptimizer.entity.User;
import com.resumeoptimizer.repository.JobDescriptionRepository;
import com.resumeoptimizer.repository.UserRepository;
import com.resumeoptimizer.security.CustomUserDetails;
import com.resumeoptimizer.service.document.DocxParserService;
import com.resumeoptimizer.service.document.PdfParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/job-descriptions")
@RequiredArgsConstructor
public class JobDescriptionController {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserRepository userRepository;
    private final PdfParserService pdfParserService;
    private final DocxParserService docxParserService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadJobDescription(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "text", required = false) String text,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (file == null && (text == null || text.trim().isEmpty())) {
            return ResponseEntity.badRequest().body(new UploadResponse(null, null, "Must provide either a file or text content"));
        }

        String fileName = null;
        String sourceType = "TEXT";
        String extractedText = text != null ? text : "";
        byte[] fileContent = null;

        if (file != null && !file.isEmpty()) {
            fileName = file.getOriginalFilename();
            fileContent = file.getBytes();
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(".pdf")) {
                    sourceType = "PDF";
                    extractedText = pdfParserService.parsePdf(file.getInputStream());
                } else if (fileName.toLowerCase().endsWith(".docx")) {
                    sourceType = "DOCX";
                    extractedText = docxParserService.parseDocx(file.getInputStream());
                } else if (fileName.toLowerCase().endsWith(".txt")) {
                    sourceType = "TEXT";
                    extractedText = new String(file.getBytes());
                } else {
                    return ResponseEntity.badRequest().body(new UploadResponse(null, fileName, "Unsupported file type"));
                }
            }
        }

        JobDescription jd = JobDescription.builder()
                .user(user)
                .fileName(fileName)
                .sourceType(sourceType)
                .fileContent(fileContent)
                .rawText(extractedText)
                .parsed(false)
                .build();

        jd = jobDescriptionRepository.save(jd);

        return ResponseEntity.ok(new UploadResponse(jd.getId(), fileName, "Job Description uploaded successfully"));
    }

    @GetMapping
    public ResponseEntity<List<JobDescription>> getJobDescriptions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<JobDescription> jds = jobDescriptionRepository.findByUserId(userDetails.getUser().getId());
        return ResponseEntity.ok(jds);
    }
}
