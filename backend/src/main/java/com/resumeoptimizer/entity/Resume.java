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
import java.util.Map;
import java.util.List;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "storage_reference")
    private String storageReference;

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "professional_summary", columnDefinition = "TEXT")
    private String professionalSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "experience_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> experience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "virtual_experience_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> virtualExperience;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skills_json", columnDefinition = "jsonb")
    private List<String> skills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "education_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> education;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "projects_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> projects;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "certifications_json", columnDefinition = "jsonb")
    private List<Map<String, Object>> certifications;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "achievements_json", columnDefinition = "jsonb")
    private List<String> achievements;

    @Column(nullable = false)
    private Boolean parsed = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
