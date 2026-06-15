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

@Entity
@Table(name = "job_descriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title")
    private String title;

    @Column(name = "company")
    private String company;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "file_name")
    private String fileName;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "file_content")
    private byte[] fileContent;

    @Column(name = "raw_text", nullable = false, columnDefinition = "TEXT")
    private String rawText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "required_skills_json", columnDefinition = "jsonb")
    private List<String> requiredSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferred_skills_json", columnDefinition = "jsonb")
    private List<String> preferredSkills;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "technologies_json", columnDefinition = "jsonb")
    private List<String> technologies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "responsibilities_json", columnDefinition = "jsonb")
    private List<String> responsibilities;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keywords_json", columnDefinition = "jsonb")
    private List<String> keywords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "domain_knowledge_json", columnDefinition = "jsonb")
    private List<String> domainKnowledge;

    @Column(nullable = false)
    private Boolean parsed = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
