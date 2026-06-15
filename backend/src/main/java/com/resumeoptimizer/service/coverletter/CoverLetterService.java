package com.resumeoptimizer.service.coverletter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeoptimizer.dto.request.CoverLetterRequest;
import com.resumeoptimizer.dto.response.CoverLetterResponse;
import com.resumeoptimizer.entity.CoverLetter;
import com.resumeoptimizer.entity.JobDescription;
import com.resumeoptimizer.entity.Resume;
import com.resumeoptimizer.entity.User;
import com.resumeoptimizer.exception.AiServiceException;
import com.resumeoptimizer.repository.CoverLetterRepository;
import com.resumeoptimizer.repository.JobDescriptionRepository;
import com.resumeoptimizer.repository.ResumeRepository;
import com.resumeoptimizer.service.ai.AiClientService;
import com.resumeoptimizer.service.ai.CoverLetterPromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final AiClientService aiClientService;
    private final CoverLetterPromptService promptService;
    private final ObjectMapper objectMapper;

    @Transactional
    public CoverLetterResponse generateCoverLetter(User user, CoverLetterRequest request) {
        Resume resume = resumeRepository.findById(request.getResumeId())
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        if (!resume.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to access this resume");
        }

        JobDescription jd = null;
        if (request.getJobDescriptionId() != null) {
            jd = jobDescriptionRepository.findById(request.getJobDescriptionId())
                    .orElse(null);
            if (jd != null && !jd.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to access this job description");
            }
        }

        String systemPrompt = promptService.buildSystemPrompt();
        String userPrompt = promptService.buildUserPrompt(resume, jd, request.getCompanyName(), request.getRoleTitle());

        String aiResponse = aiClientService.generateResponse(systemPrompt, userPrompt, true);

        try {
            Map<String, Object> responseMap = objectMapper.readValue(aiResponse, new TypeReference<>() {});
            String coverLetterBody = (String) responseMap.get("coverLetterBody");
            Map<String, Object> traceability = (Map<String, Object>) responseMap.get("traceability");

            // Integrity Guard
            validateTraceability(resume, traceability);

            CoverLetter coverLetter = coverLetterRepository.findByUserIdAndCompanyNameAndRoleTitle(
                    user.getId(), request.getCompanyName(), request.getRoleTitle()
            ).orElse(null);

            if (coverLetter == null) {
                coverLetter = CoverLetter.builder()
                        .user(user)
                        .resume(resume)
                        .jobDescription(jd)
                        .companyName(request.getCompanyName())
                        .roleTitle(request.getRoleTitle())
                        .build();
                coverLetter = coverLetterRepository.save(coverLetter);
            }

            // Determine next version number
            int nextVersion = 1;
            List<com.resumeoptimizer.entity.CoverLetterVersion> existingVersions = 
                new java.util.ArrayList<>();
            if (coverLetter.getId() != null) {
                // Fetch existing versions manually just to be safe if lazy loaded collection isn't updated
                existingVersions = coverLetter.getVersions();
                if (existingVersions != null && !existingVersions.isEmpty()) {
                    nextVersion = existingVersions.stream()
                            .mapToInt(com.resumeoptimizer.entity.CoverLetterVersion::getVersionNumber)
                            .max().orElse(0) + 1;
                }
            }

            com.resumeoptimizer.entity.CoverLetterVersion version = com.resumeoptimizer.entity.CoverLetterVersion.builder()
                    .coverLetter(coverLetter)
                    .versionNumber(nextVersion)
                    .versionNotes(request.getVersionNotes())
                    .generatedContent(coverLetterBody)
                    .traceability(traceability)
                    .build();

            if (coverLetter.getVersions() == null) {
                coverLetter.setVersions(new java.util.ArrayList<>());
            }
            coverLetter.getVersions().add(version);

            coverLetterRepository.save(coverLetter); // cascades save to version

            // Construct Response
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

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse or validate cover letter: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validateTraceability(Resume resume, Map<String, Object> traceability) {
        List<String> refExps = (List<String>) traceability.getOrDefault("referencedExperiences", List.of());
        List<String> refProjects = (List<String>) traceability.getOrDefault("referencedProjects", List.of());
        List<String> refTech = (List<String>) traceability.getOrDefault("referencedTechnologies", List.of());
        List<String> refMetrics = (List<String>) traceability.getOrDefault("referencedMetrics", List.of());
        List<String> refCerts = (List<String>) traceability.getOrDefault("referencedCertifications", List.of());
        List<String> refAchievements = (List<String>) traceability.getOrDefault("referencedAchievements", List.of());

        // Extract truth sets
        String resumeRaw = resume.getRawText() != null ? resume.getRawText().toLowerCase() : "";
        String experienceJsonStr = "";
        try {
            experienceJsonStr = objectMapper.writeValueAsString(resume.getExperience()).toLowerCase();
            experienceJsonStr += objectMapper.writeValueAsString(resume.getVirtualExperience()).toLowerCase();
            experienceJsonStr += objectMapper.writeValueAsString(resume.getProjects()).toLowerCase();
            experienceJsonStr += objectMapper.writeValueAsString(resume.getCertifications()).toLowerCase();
            experienceJsonStr += objectMapper.writeValueAsString(resume.getAchievements()).toLowerCase();
            experienceJsonStr += objectMapper.writeValueAsString(resume.getSkills()).toLowerCase();
        } catch (Exception ignored) {}

        String truthSource = resumeRaw + " " + experienceJsonStr;

        for (String exp : refExps) {
            if (!containsFuzzy(truthSource, exp)) {
                log.error("Validation failed: Invented experience: {}", exp);
                throw new AiServiceException("COVER_LETTER_VALIDATION_FAILED");
            }
        }
        for (String proj : refProjects) {
            if (!containsFuzzy(truthSource, proj)) {
                log.error("Validation failed: Invented project: {}", proj);
                throw new AiServiceException("COVER_LETTER_VALIDATION_FAILED");
            }
        }
        for (String tech : refTech) {
            if (!containsFuzzy(truthSource, tech)) {
                log.error("Validation failed: Invented technology: {}", tech);
                throw new AiServiceException("COVER_LETTER_VALIDATION_FAILED");
            }
        }
        for (String metric : refMetrics) {
            if (!containsFuzzy(truthSource, metric)) {
                log.error("Validation failed: Invented metric: {}", metric);
                throw new AiServiceException("COVER_LETTER_VALIDATION_FAILED");
            }
        }
        for (String cert : refCerts) {
            if (!containsFuzzy(truthSource, cert)) {
                log.error("Validation failed: Invented certification: {}", cert);
                throw new AiServiceException("COVER_LETTER_VALIDATION_FAILED");
            }
        }
        for (String ach : refAchievements) {
            if (!containsFuzzy(truthSource, ach)) {
                log.error("Validation failed: Invented achievement: {}", ach);
                throw new AiServiceException("COVER_LETTER_VALIDATION_FAILED");
            }
        }
    }

    private boolean containsFuzzy(String source, String query) {
        if (query == null || query.isBlank()) return true;
        
        // Strip out common punctuation and convert to lowercase for fuzzy matching
        String cleanSource = source.replaceAll("[^a-zA-Z0-9\\\\s]", " ").replaceAll("\\\\s+", " ");
        String cleanQuery = query.toLowerCase().replaceAll("[^a-zA-Z0-9\\\\s]", " ").replaceAll("\\\\s+", " ").trim();
        
        if (cleanQuery.isBlank()) return true;
        
        // A simple word-based subset check. The query words must appear in the source.
        String[] queryWords = cleanQuery.split(" ");
        for (String word : queryWords) {
            if (word.length() > 2 && !cleanSource.contains(word)) {
                return false;
            }
        }
        return true;
    }
}
