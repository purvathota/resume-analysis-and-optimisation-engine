package com.resumeoptimizer.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "analyses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private JobDescription jobDescription;

    // ATS Results
    @Column(name = "ats_score")
    private Integer atsScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_technical_keywords", columnDefinition = "jsonb")
    private List<String> missingTechnicalKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_soft_skills", columnDefinition = "jsonb")
    private List<String> missingSoftSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_domain_keywords", columnDefinition = "jsonb")
    private List<String> missingDomainKeywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_certifications", columnDefinition = "jsonb")
    private List<String> missingCertifications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_tooling", columnDefinition = "jsonb")
    private List<String> missingTooling;

    @Column(name = "ats_raw_response", columnDefinition = "TEXT")
    private String atsRawResponse;

    // Recruiter Results
    @Column(name = "recruiter_fit_score")
    private Integer recruiterFitScore;

    @Column(name = "shortlisting_probability")
    private String shortlistingProbability;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "strengths_json", columnDefinition = "jsonb")
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "weaknesses_json", columnDefinition = "jsonb")
    private List<String> weaknesses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "improvement_suggestions_json", columnDefinition = "jsonb")
    private List<Map<String, String>> improvementSuggestions;

    @Column(name = "recruiter_raw_response", columnDefinition = "TEXT")
    private String recruiterRawResponse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "optimized_resume_json", columnDefinition = "jsonb")
    private Map<String, Object> optimizedResumeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keyword_improvement_report_json", columnDefinition = "jsonb")
    private Map<String, Object> keywordImprovementReportJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resume_diff_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> resumeDiffJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_skills_json", columnDefinition = "jsonb")
    private List<String> missingSkillsJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_report_json", columnDefinition = "jsonb")
    private Map<String, Object> validationReportJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ats_impact_report_json", columnDefinition = "jsonb")
    private Map<String, Object> atsImpactReportJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "original_resume_json", columnDefinition = "jsonb")
    private Map<String, Object> originalResumeJson;

    @Column(name = "analysis_status")
    private String analysisStatus;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
