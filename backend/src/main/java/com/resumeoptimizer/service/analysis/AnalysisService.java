package com.resumeoptimizer.service.analysis;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeoptimizer.entity.Analysis;
import com.resumeoptimizer.entity.JobDescription;
import com.resumeoptimizer.entity.Resume;
import com.resumeoptimizer.repository.AnalysisRepository;
import com.resumeoptimizer.repository.JobDescriptionRepository;
import com.resumeoptimizer.repository.ResumeRepository;
import com.resumeoptimizer.service.ai.AiClientService;
import com.resumeoptimizer.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final AiClientService aiClientService;
    private final PromptTemplateService promptTemplateService;
    private final ObjectMapper objectMapper;
    private final ResumeValidationService resumeValidationService;

    public Analysis performAtsAnalysis(Long resumeId, Long jobDescriptionId) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new RuntimeException("Job Description not found"));

        Analysis analysis = analysisRepository.findByResumeIdAndJobDescriptionId(resumeId, jobDescriptionId)
                .orElse(Analysis.builder()
                        .resume(resume)
                        .jobDescription(jd)
                        .analysisStatus("ATS_IN_PROGRESS")
                        .build());

        String systemPrompt = promptTemplateService.getAtsAnalysisSystemPrompt();
        String userPrompt = promptTemplateService.buildUserPrompt(resume.getRawText(), jd.getRawText());

        String rawResponse = aiClientService.generateResponse(systemPrompt, userPrompt, true);
        analysis.setAtsRawResponse(rawResponse);

        // Parse JSON
        Map<String, Object> jsonResponse = objectMapper.readValue(rawResponse, new TypeReference<>() {});
        
        analysis.setAtsScore((Integer) jsonResponse.get("atsScore"));
        analysis.setMissingTechnicalKeywords((List<String>) jsonResponse.get("missingTechnicalKeywords"));
        analysis.setMissingSoftSkills((List<String>) jsonResponse.get("missingSoftSkills"));
        analysis.setMissingDomainKeywords((List<String>) jsonResponse.get("missingDomainKeywords"));
        analysis.setMissingCertifications((List<String>) jsonResponse.get("missingCertifications"));
        analysis.setMissingTooling((List<String>) jsonResponse.get("missingTooling"));
        
        analysis.setAnalysisStatus("ATS_COMPLETED");

        return analysisRepository.save(analysis);
    }

    public Analysis performRecruiterReview(Long resumeId, Long jobDescriptionId) throws Exception {
        Analysis analysis = analysisRepository.findByResumeIdAndJobDescriptionId(resumeId, jobDescriptionId)
                .orElseThrow(() -> new RuntimeException("ATS Analysis must be performed first"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new RuntimeException("Job Description not found"));

        String systemPrompt = promptTemplateService.getRecruiterReviewSystemPrompt();
        String userPrompt = promptTemplateService.buildUserPrompt(resume.getRawText(), jd.getRawText());

        String rawResponse = aiClientService.generateResponse(systemPrompt, userPrompt, true);
        analysis.setRecruiterRawResponse(rawResponse);

        // Parse JSON
        Map<String, Object> jsonResponse = objectMapper.readValue(rawResponse, new TypeReference<>() {});

        analysis.setRecruiterFitScore((Integer) jsonResponse.get("recruiterFitScore"));
        analysis.setShortlistingProbability((String) jsonResponse.get("shortlistingProbability"));
        analysis.setStrengths((List<String>) jsonResponse.get("strengths"));
        analysis.setWeaknesses((List<String>) jsonResponse.get("weaknesses"));
        analysis.setImprovementSuggestions((List<Map<String, String>>) jsonResponse.get("improvementSuggestions"));

        analysis.setAnalysisStatus("RECRUITER_COMPLETED");
        return analysisRepository.save(analysis);
    }

    public Analysis performResumeOptimization(Long resumeId, Long jobDescriptionId) throws Exception {
        Analysis analysis = analysisRepository.findByResumeIdAndJobDescriptionId(resumeId, jobDescriptionId)
                .orElseThrow(() -> new RuntimeException("Recruiter Review must be performed first"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        JobDescription jd = jobDescriptionRepository.findById(jobDescriptionId)
                .orElseThrow(() -> new RuntimeException("Job Description not found"));

        String systemPrompt = promptTemplateService.getResumeOptimizationSystemPrompt();
        String userPrompt = promptTemplateService.buildUserPrompt(resume.getRawText(), jd.getRawText());

        String rawResponse = aiClientService.generateResponse(systemPrompt, userPrompt, true);
        
        // Parse JSON
        Map<String, Object> jsonResponse = objectMapper.readValue(rawResponse, new TypeReference<>() {});

        Map<String, Object> optimizedResume = (Map<String, Object>) jsonResponse.get("optimizedResume");
        Map<String, Object> keywordImprovementReport = (Map<String, Object>) jsonResponse.get("keywordImprovementReport");
        List<Map<String, Object>> resumeDiff = (List<Map<String, Object>>) jsonResponse.get("resumeDiff");
        List<String> missingSkills = (List<String>) jsonResponse.get("missingSkills");

        // 1. Build originalResumeJson for snapshot
        Map<String, Object> originalResumeJson = new java.util.HashMap<>();
        originalResumeJson.put("professionalSummary", resume.getProfessionalSummary());
        originalResumeJson.put("professionalExperience", resume.getExperience());
        originalResumeJson.put("skills", resume.getSkills());
        originalResumeJson.put("education", resume.getEducation());
        originalResumeJson.put("projects", resume.getProjects());
        originalResumeJson.put("certifications", resume.getCertifications());
        originalResumeJson.put("achievements", resume.getAchievements());
        
        // 2. Validate
        Map<String, Object> validationReport = resumeValidationService.validateOptimization(resume, optimizedResume);
        
        // 3. Re-run ATS on Optimized Resume
        String atsSystemPrompt = promptTemplateService.getAtsAnalysisSystemPrompt();
        String atsUserPrompt = promptTemplateService.buildUserPrompt(objectMapper.writeValueAsString(optimizedResume), jd.getRawText());
        String atsRawResponse = aiClientService.generateResponse(atsSystemPrompt, atsUserPrompt, true);
        Map<String, Object> optimizedAtsJson = objectMapper.readValue(atsRawResponse, new TypeReference<>() {});
        
        int originalAtsScore = analysis.getAtsScore() != null ? analysis.getAtsScore() : 0;
        int optimizedAtsScore = (Integer) optimizedAtsJson.get("atsScore");
        
        Map<String, Object> atsImpactReport = new java.util.HashMap<>();
        atsImpactReport.put("originalAtsScore", originalAtsScore);
        atsImpactReport.put("optimizedAtsScore", optimizedAtsScore);
        atsImpactReport.put("atsImprovementPercentage", optimizedAtsScore - originalAtsScore);
        atsImpactReport.put("keywordsAdded", keywordImprovementReport.get("keywordsAdded"));
        atsImpactReport.put("unsupportedKeywordsNotAdded", missingSkills);
        
        analysis.setOptimizedResumeJson(optimizedResume);
        analysis.setKeywordImprovementReportJson(keywordImprovementReport);
        analysis.setResumeDiffJson(resumeDiff);
        analysis.setMissingSkillsJson(missingSkills);
        analysis.setOriginalResumeJson(originalResumeJson);
        analysis.setValidationReportJson(validationReport);
        analysis.setAtsImpactReportJson(atsImpactReport);

        Boolean isValidForExport = (Boolean) validationReport.get("isValidForExport");
        if (isValidForExport != null && !isValidForExport) {
            analysis.setAnalysisStatus("OPTIMIZATION_FAILED_VALIDATION");
        } else {
            analysis.setAnalysisStatus("OPTIMIZATION_COMPLETED");
        }

        return analysisRepository.save(analysis);
    }
}
