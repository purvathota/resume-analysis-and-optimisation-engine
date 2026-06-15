package com.resumeoptimizer.controller;

import com.resumeoptimizer.dto.response.UploadResponse;
import com.resumeoptimizer.entity.Resume;
import com.resumeoptimizer.entity.User;
import com.resumeoptimizer.repository.ResumeRepository;
import com.resumeoptimizer.repository.UserRepository;
import com.resumeoptimizer.security.CustomUserDetails;
import com.resumeoptimizer.service.document.DocxParserService;
import com.resumeoptimizer.service.document.PdfParserService;
import com.resumeoptimizer.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final PdfParserService pdfParserService;
    private final DocxParserService docxParserService;

    private final StorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {

        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = file.getOriginalFilename();
        String fileType = "UNKNOWN";
        String extractedText = "";

        if (fileName != null) {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                fileType = "PDF";
                extractedText = pdfParserService.parsePdf(file.getInputStream());
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                fileType = "DOCX";
                extractedText = docxParserService.parseDocx(file.getInputStream());
            } else {
                return ResponseEntity.badRequest().body(new UploadResponse(null, fileName, "Only PDF and DOCX are supported"));
            }
        }

        // Upload to Storage Provider
        String storageReference = storageService.uploadFile(fileName, file.getBytes(), file.getContentType());

        Resume resume = Resume.builder()
                .user(user)
                .fileName(fileName)
                .fileType(fileType)
                .storageReference(storageReference)
                .rawText(extractedText)
                .parsed(false)
                .build();

        resume = resumeRepository.save(resume);

        return ResponseEntity.ok(new UploadResponse(resume.getId(), fileName, "Resume uploaded successfully"));
    }

    @GetMapping
    public ResponseEntity<List<Resume>> getResumes(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Resume> resumes = resumeRepository.findByUserId(userDetails.getUser().getId());
        return ResponseEntity.ok(resumes);
    }
}
