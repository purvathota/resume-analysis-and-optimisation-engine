package com.resumeoptimizer.controller;

import com.resumeoptimizer.dto.request.CoverLetterRequest;
import com.resumeoptimizer.dto.response.CoverLetterResponse;
import com.resumeoptimizer.entity.CoverLetter;
import com.resumeoptimizer.entity.User;
import com.resumeoptimizer.repository.CoverLetterRepository;
import com.resumeoptimizer.repository.UserRepository;
import com.resumeoptimizer.security.CustomUserDetails;
import com.resumeoptimizer.service.coverletter.CoverLetterService;
import com.resumeoptimizer.service.document.DocumentExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;
    private final CoverLetterRepository coverLetterRepository;
    private final UserRepository userRepository;
    private final DocumentExportService documentExportService;

    @PostMapping("/generate")
    public ResponseEntity<CoverLetterResponse> generateCoverLetter(
            @Valid @RequestBody CoverLetterRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        User user = userRepository.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        CoverLetterResponse response = coverLetterService.generateCoverLetter(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CoverLetterResponse>> getUserCoverLetters(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CoverLetter> list = coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getUser().getId());
        List<CoverLetterResponse> resList = list.stream().map(coverLetter -> {
            List<com.resumeoptimizer.dto.response.CoverLetterVersionResponse> vResponses = coverLetter.getVersions().stream()
                    .map(v -> com.resumeoptimizer.dto.response.CoverLetterVersionResponse.builder()
                            .id(v.getId())
                            .versionNumber(v.getVersionNumber())
                            .versionNotes(v.getVersionNotes())
                            .generatedContent(v.getGeneratedContent())
                            .traceability(v.getTraceability())
                            .createdAt(v.getCreatedAt())
                            .build())
                    .toList();
            return CoverLetterResponse.builder()
                    .id(coverLetter.getId())
                    .companyName(coverLetter.getCompanyName())
                    .roleTitle(coverLetter.getRoleTitle())
                    .versions(vResponses)
                    .createdAt(coverLetter.getCreatedAt())
                    .build();
        }).toList();
        return ResponseEntity.ok(resList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CoverLetterResponse> getCoverLetter(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        CoverLetter coverLetter = coverLetterRepository.findByIdAndUserId(id, userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Cover letter not found"));
        
        List<com.resumeoptimizer.dto.response.CoverLetterVersionResponse> vResponses = coverLetter.getVersions().stream()
                .map(v -> com.resumeoptimizer.dto.response.CoverLetterVersionResponse.builder()
                        .id(v.getId())
                        .versionNumber(v.getVersionNumber())
                        .versionNotes(v.getVersionNotes())
                        .generatedContent(v.getGeneratedContent())
                        .traceability(v.getTraceability())
                        .createdAt(v.getCreatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(CoverLetterResponse.builder()
                .id(coverLetter.getId())
                .companyName(coverLetter.getCompanyName())
                .roleTitle(coverLetter.getRoleTitle())
                .versions(vResponses)
                .createdAt(coverLetter.getCreatedAt())
                .build());
    }

    @GetMapping("/versions/{versionId}/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable Long versionId, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        com.resumeoptimizer.entity.CoverLetterVersion clv = coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getUser().getId()).stream()
                .flatMap(cl -> cl.getVersions().stream())
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cover letter version not found"));
                
        byte[] pdfBytes = documentExportService.generateCoverLetterPdf(clv);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"CoverLetter_" + clv.getCoverLetter().getCompanyName() + "_V" + clv.getVersionNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/versions/{versionId}/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable Long versionId, @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        com.resumeoptimizer.entity.CoverLetterVersion clv = coverLetterRepository.findByUserIdOrderByCreatedAtDesc(userDetails.getUser().getId()).stream()
                .flatMap(cl -> cl.getVersions().stream())
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cover letter version not found"));
                
        byte[] docxBytes = documentExportService.generateCoverLetterDocx(clv);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"CoverLetter_" + clv.getCoverLetter().getCompanyName() + "_V" + clv.getVersionNumber() + ".docx\"")
                .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(docxBytes);
    }
}
