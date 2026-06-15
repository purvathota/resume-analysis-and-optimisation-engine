package com.resumeoptimizer.service.ai;

import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String getAtsAnalysisSystemPrompt() {
        return "You are an expert ATS (Applicant Tracking System) emulator. " +
               "Compare the provided Resume with the provided Job Description. " +
               "You must return the result as a strict JSON object with the following structure: " +
               "{ \"atsScore\": integer (0-100), \"missingTechnicalKeywords\": [string], " +
               "\"missingSoftSkills\": [string], \"missingDomainKeywords\": [string], " +
               "\"missingCertifications\": [string], \"missingTooling\": [string] }";
    }

    public String getRecruiterReviewSystemPrompt() {
        return "You are an expert tech recruiter in the UK. " +
               "Analyze the candidate's Resume against the Job Description. " +
               "You must return the result as a strict JSON object with the following structure: " +
               "{ \"recruiterFitScore\": integer (0-100), \"shortlistingProbability\": \"HIGH\"|\"MEDIUM\"|\"LOW\", " +
               "\"strengths\": [string], \"weaknesses\": [string], " +
               "\"improvementSuggestions\": [{ \"section\": \"string\", \"suggestion\": \"string\" }] }";
    }

    public String getResumeOptimizationSystemPrompt() {
        return "You are an expert tech Resume Tailoring Engine. Your goal is to optimize the provided Resume " +
               "for the provided Job Description. The uploaded resume is the Master Resume and the single source of truth.\n\n" +
               "TRUTHFULNESS ENGINE V2 (STRICT RULES):\n" +
               "1. Optimization Budget: Modify a MAXIMUM of 10-15% of the resume content. The goal is 95% preservation, 5% ATS enhancement.\n" +
               "2. NEVER invent numerical values, percentages, performance metrics, user counts, or transaction volumes not present in the master resume.\n" +
               "3. NEVER invent technologies not present in the master resume.\n" +
               "4. NEVER invent projects, responsibilities, certifications, achievements, or leadership experience.\n" +
               "5. NEVER modify employment dates, company names, or job titles.\n" +
               "6. Header Preservation: The fullName, title, location, phone, email, linkedIn, gitHub, and leetCode must be 100% perfectly preserved exactly as they appear.\n\n" +
               "ALLOWED OPTIMIZATIONS:\n" +
               "1. Reorder existing skills and bullet points based on job description relevance.\n" +
               "2. Rewrite existing bullets using stronger wording while preserving original meaning.\n" +
               "3. Replace synonyms with terminology used in the job description.\n" +
               "4. Improve ATS keyword alignment using keywords already supported by the candidate's experience.\n" +
               "5. Improve formatting and readability.\n" +
               "6. Highlight relevant projects and skills already present in the resume.\n\n" +
               "You must output a strict JSON object with four root keys: 'optimizedResume', 'keywordImprovementReport', 'resumeDiff', and 'missingSkills'.\n" +
               "If a required keyword from the JD is not supported by the candidate's actual experience, list it ONLY in the 'missingSkills' array and NEVER add it to the resume.\n\n" +
               "The 'optimizedResume' must perfectly match this structure:\n" +
               "{ \"header\": { \"fullName\": \"string\", \"title\": \"string\", \"location\": \"string\", \"phone\": \"string\", \"email\": \"string\", \"linkedIn\": \"string\", \"gitHub\": \"string\", \"leetCode\": \"string\" },\n" +
               "  \"professionalSummary\": \"string\",\n" +
               "  \"professionalExperience\": [ { \"title\": \"string\", \"company\": \"string\", \"dates\": \"string\", \"location\": \"string\", \"bullets\": [\"string\"] } ],\n" +
               "  \"virtualExperience\": [ { \"title\": \"string\", \"company\": \"string\", \"dates\": \"string\", \"location\": \"string\", \"bullets\": [\"string\"] } ],\n" +
               "  \"skills\": [ { \"category\": \"string\", \"items\": \"string\" } ],\n" +
               "  \"education\": [ { \"degree\": \"string\", \"university\": \"string\", \"dates\": \"string\", \"location\": \"string\", \"details\": \"string\" } ],\n" +
               "  \"projects\": [ { \"title\": \"string\", \"link\": \"string\", \"techStack\": \"string\", \"bullets\": [\"string\"] } ],\n" +
               "  \"certifications\": [ { \"title\": \"string\", \"date\": \"string\", \"bullets\": [\"string\"] } ],\n" +
               "  \"achievements\": [\"string\"] }\n\n" +
               "The 'resumeDiff' must be an array tracking every single modified bullet or significant change. Structure:\n" +
               "[ { \"originalBullet\": \"string\", \"optimizedBullet\": \"string\", \"reason\": \"string\", \"keywordsAdded\": [\"string\"],\n" +
               "\"changeType\": \"KEYWORD_ALIGNMENT|REORDERING|FORMATTING|SUMMARY_ENHANCEMENT|BULLET_REWRITE|SKILL_REORDERING\", \"confidenceScore\": integer (0-100) } ]\n\n" +
               "The 'missingSkills' must be an array of strings: [\"string\"].\n\n" +
               "The 'keywordImprovementReport' must match:\n" +
               "{ \"changesMade\": [\"string\"], \"keywordsAdded\": [\"string\"], \"skillsReordered\": [\"string\"], \"experienceImprovements\": [\"string\"] }";
    }

    public String buildUserPrompt(String resumeText, String jobDescriptionText) {
        return "--- RESUME ---\n" + resumeText + "\n\n" +
               "--- JOB DESCRIPTION ---\n" + jobDescriptionText;
    }
}
