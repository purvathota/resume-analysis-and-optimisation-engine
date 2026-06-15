package com.resumeoptimizer.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeoptimizer.entity.JobDescription;
import com.resumeoptimizer.entity.Resume;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CoverLetterPromptService {

    private final ObjectMapper objectMapper;

    public String buildSystemPrompt() {
        return """
               You are an expert executive recruiter writing a highly professional, strictly truthful cover letter.
               
               CRITICAL TRUTHFULNESS RULES:
               - 95% Truthfulness, 5% Tailoring.
               - You must NEVER invent or hallucinate Experience, Projects, Technologies, Certifications, Achievements, Metrics, or Responsibilities.
               - Every single claim in the cover letter MUST be directly traceable to the provided Resume context.
               - If the resume does not contain a requested skill or experience, DO NOT invent it.
               
               TONE RULES:
               - Use exactly ONE recruiter-grade professional tone.
               - Do NOT be overly friendly, casual, enthusiastic, or creative.
               - Keep it concise, authoritative, and direct (suitable for Fintech, Product, or Investment Tech firms).
               
               COMPANY ALIGNMENT CONTEXT:
               - You may reference the company's presumed domain/goals (e.g., if the company is Wise, you may reference global payments).
               - You must map the candidate's existing truthful experience to this domain.
               
               OUTPUT FORMAT:
               You MUST output valid JSON conforming EXACTLY to the following schema:
               {
                 "coverLetterBody": "The actual text of the cover letter...",
                 "traceability": {
                   "referencedExperiences": ["Exact job title/company strings from the resume"],
                   "referencedProjects": ["Exact project names from the resume"],
                   "referencedTechnologies": ["Exact technology names from the resume"],
                   "referencedMetrics": ["Exact metrics from the resume"],
                   "referencedCertifications": ["Exact certifications from the resume"],
                   "referencedAchievements": ["Exact achievements from the resume"]
                 }
               }
               
               Do NOT include sender/recipient addresses in the body. Do NOT include Date. Do NOT include Name/Email headers. The system will prepend these automatically.
               Start the body directly with "Dear Hiring Manager," and end with "Kind Regards,\\n[Candidate Name]".
               """;
    }

    public String buildUserPrompt(Resume resume, JobDescription jobDescription, String companyName, String roleTitle) {
        Map<String, Object> context = new HashMap<>();
        context.put("targetCompany", companyName);
        context.put("targetRole", roleTitle);
        
        if (jobDescription != null && jobDescription.getRawText() != null) {
            context.put("jobDescription", jobDescription.getRawText());
        }

        Map<String, Object> resumeContext = new HashMap<>();
        resumeContext.put("professionalSummary", resume.getProfessionalSummary());
        resumeContext.put("experience", resume.getExperience());
        resumeContext.put("virtualExperience", resume.getVirtualExperience());
        resumeContext.put("projects", resume.getProjects());
        resumeContext.put("skills", resume.getSkills());
        resumeContext.put("certifications", resume.getCertifications());
        resumeContext.put("achievements", resume.getAchievements());

        context.put("candidateResume", resumeContext);

        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize context for prompt", e);
        }
    }
}
