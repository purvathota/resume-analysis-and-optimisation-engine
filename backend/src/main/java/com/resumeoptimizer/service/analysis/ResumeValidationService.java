package com.resumeoptimizer.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeoptimizer.entity.Analysis;
import com.resumeoptimizer.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

@Service
public class ResumeValidationService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> validateOptimization(Resume originalResume, Map<String, Object> optimizedJson) throws Exception {
        Map<String, Object> report = new HashMap<>();
        String rawOriginal = originalResume.getRawText() != null ? originalResume.getRawText() : "";
        String stringifiedOptimized = objectMapper.writeValueAsString(optimizedJson);

        int headerScore = calculateHeaderIntegrity(rawOriginal, optimizedJson, originalResume.getUser());
        int sectionScore = calculateSectionPreservation(originalResume, optimizedJson);
        int metricScore = calculateMetricPreservation(rawOriginal, stringifiedOptimized);
        int techScore = calculateTechnologyPreservation(originalResume, stringifiedOptimized);
        int structureScore = calculateStructureIntegrity(originalResume, optimizedJson);
        int linkScore = calculateHyperlinkIntegrity(rawOriginal, optimizedJson);
        
        // Truthfulness is a strict 100 if all others are high.
        int truthfulnessScore = (headerScore + metricScore + techScore) / 3;
        
        int overallScore = (headerScore + sectionScore + metricScore + techScore + structureScore + linkScore + truthfulnessScore) / 7;

        boolean isValidForExport = (headerScore == 100) && (sectionScore == 100) && (metricScore >= 95) && (techScore >= 95);

        report.put("headerIntegrityScore", headerScore);
        report.put("sectionPreservationScore", sectionScore);
        report.put("metricPreservationScore", metricScore);
        report.put("technologyPreservationScore", techScore);
        report.put("hyperlinkIntegrityScore", linkScore);
        report.put("structureIntegrityScore", structureScore);
        report.put("truthfulnessScore", truthfulnessScore);
        report.put("overallResumeIntegrityScore", overallScore);
        report.put("isValidForExport", isValidForExport);

        return report;
    }

    private int calculateHeaderIntegrity(String rawOriginal, Map<String, Object> optimizedJson, com.resumeoptimizer.entity.User user) {
        if (!optimizedJson.containsKey("header")) return 0;
        Map<String, String> header = (Map<String, String>) optimizedJson.get("header");
        
        int matchCount = 0;
        int totalFields = 0;

        String[] fields = {"fullName", "email", "phone", "linkedIn", "gitHub", "leetCode", "location"};
        for (String field : fields) {
            String val = header.get(field);
            if (val != null && !val.trim().isEmpty() && !val.equalsIgnoreCase("N/A") && !val.equalsIgnoreCase("Not Provided")) {
                totalFields++;
                if (field.equals("email") && val.equalsIgnoreCase(user.getEmail())) {
                    matchCount++;
                } else if (field.equals("fullName") && val.equalsIgnoreCase(user.getFullName())) {
                    matchCount++;
                } else if (rawOriginal.contains(val)) {
                    matchCount++;
                }
            }
        }
        
        return totalFields == 0 ? 100 : (int) (((double) matchCount / totalFields) * 100);
    }

    private int calculateSectionPreservation(Resume originalResume, Map<String, Object> optimizedJson) {
        int expected = 0;
        int found = 0;

        if (originalResume.getExperience() != null && !originalResume.getExperience().isEmpty()) {
            expected++;
            if (optimizedJson.containsKey("professionalExperience")) found++;
        }
        if (originalResume.getEducation() != null && !originalResume.getEducation().isEmpty()) {
            expected++;
            if (optimizedJson.containsKey("education")) found++;
        }
        if (originalResume.getSkills() != null && !originalResume.getSkills().isEmpty()) {
            expected++;
            if (optimizedJson.containsKey("skills")) found++;
        }
        if (originalResume.getProjects() != null && !originalResume.getProjects().isEmpty()) {
            expected++;
            if (optimizedJson.containsKey("projects")) found++;
        }
        if (originalResume.getCertifications() != null && !originalResume.getCertifications().isEmpty()) {
            expected++;
            if (optimizedJson.containsKey("certifications")) found++;
        }
        if (originalResume.getAchievements() != null && !originalResume.getAchievements().isEmpty()) {
            expected++;
            if (optimizedJson.containsKey("achievements")) found++;
        }

        return expected == 0 ? 100 : (int) (((double) found / expected) * 100);
    }

    private int calculateMetricPreservation(String rawOriginal, String stringifiedOptimized) {
        Pattern pattern = Pattern.compile("\\b\\d+(?:%|k|K|m|M|\\+)?\\b");
        Matcher origMatcher = pattern.matcher(rawOriginal);
        
        List<String> originalMetrics = new ArrayList<>();
        while (origMatcher.find()) {
            String m = origMatcher.group();
            if (!m.matches("^(19|20)\\d{2}$")) {
                originalMetrics.add(m);
            }
        }

        if (originalMetrics.isEmpty()) return 100;

        Matcher optMatcher = pattern.matcher(stringifiedOptimized);
        List<String> optMetrics = new ArrayList<>();
        while (optMatcher.find()) {
            String m = optMatcher.group();
            if (!m.matches("^(19|20)\\d{2}$")) {
                optMetrics.add(m);
            }
        }

        // Percentage based on the count of preserved metrics versus original
        int preserved = 0;
        for (String metric : originalMetrics) {
            if (optMetrics.contains(metric)) {
                preserved++;
                optMetrics.remove(metric); // Remove so we don't overcount duplicates
            }
        }

        return (int) (((double) preserved / originalMetrics.size()) * 100);
    }

    private int calculateTechnologyPreservation(Resume originalResume, String stringifiedOptimized) {
        List<String> unifiedTechSet = new ArrayList<>();
        
        // 1. Extract from explicit Skills list
        if (originalResume.getSkills() != null) {
            for (String skillLine : originalResume.getSkills()) {
                String[] tokens = skillLine.split("[,\\|]");
                for (String token : tokens) {
                    if (token.trim().length() > 1) {
                        unifiedTechSet.add(token.trim());
                    }
                }
            }
        }

        // 2. Extract from Projects (techStack)
        if (originalResume.getProjects() != null) {
            for (Map<String, Object> proj : originalResume.getProjects()) {
                if (proj.containsKey("techStack") && proj.get("techStack") != null) {
                    String[] tokens = proj.get("techStack").toString().split("[,\\|]");
                    for (String token : tokens) {
                        if (token.trim().length() > 1) {
                            unifiedTechSet.add(token.trim());
                        }
                    }
                }
            }
        }
        
        // Remove duplicates and common stop words from the unified set
        List<String> finalTechSet = new ArrayList<>();
        for (String tech : unifiedTechSet) {
            String lower = tech.toLowerCase();
            if (!finalTechSet.contains(lower)) {
                finalTechSet.add(lower);
            }
        }

        if (finalTechSet.isEmpty()) return 100;

        int preserved = 0;
        String lowerOptimized = stringifiedOptimized.toLowerCase();
        for (String tech : finalTechSet) {
            // A simple substring search. In a real system, you'd use word boundaries.
            if (lowerOptimized.contains(tech)) {
                preserved++;
            }
        }

        return (int) (((double) preserved / finalTechSet.size()) * 100);
    }

    private int calculateStructureIntegrity(Resume originalResume, Map<String, Object> optimizedJson) {
        if (!optimizedJson.containsKey("professionalExperience")) return 100;
        List<Map<String, Object>> exps = (List<Map<String, Object>>) optimizedJson.get("professionalExperience");
        for (Map<String, Object> exp : exps) {
            if (!exp.containsKey("title") || !exp.containsKey("company") || !exp.containsKey("dates")) {
                return 0;
            }
        }
        return 100;
    }

    private int calculateHyperlinkIntegrity(String rawOriginal, Map<String, Object> optimizedJson) {
        int totalLinks = 0;
        int validLinks = 0;

        // Check Header Links
        if (optimizedJson.containsKey("header")) {
            Map<String, String> header = (Map<String, String>) optimizedJson.get("header");
            String[] linkFields = {"linkedIn", "gitHub", "leetCode"};
            for (String field : linkFields) {
                String val = header.get(field);
                if (val != null && !val.trim().isEmpty() && !val.equalsIgnoreCase("N/A")) {
                    totalLinks++;
                    boolean isUrl = val.contains(".") || val.contains("/");
                    boolean isOriginalText = rawOriginal.contains(val);
                    if (isUrl || isOriginalText) validLinks++;
                }
            }
        }

        // Check Project Links
        if (optimizedJson.containsKey("projects")) {
            List<Map<String, Object>> projects = (List<Map<String, Object>>) optimizedJson.get("projects");
            for (Map<String, Object> proj : projects) {
                if (proj.containsKey("link")) {
                    String link = (String) proj.get("link");
                    if (link != null && !link.trim().isEmpty() && !link.equalsIgnoreCase("Project Link")) {
                        totalLinks++;
                        boolean isUrl = link.contains(".") || link.contains("/");
                        boolean isOriginalText = rawOriginal.contains(link);
                        if (isUrl || isOriginalText) validLinks++;
                    }
                }
            }
        }

        return totalLinks == 0 ? 100 : (int) (((double) validLinks / totalLinks) * 100);
    }
}
