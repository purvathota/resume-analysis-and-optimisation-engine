package com.resumeoptimizer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

@Entity
@Table(name = "cover_letter_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverLetterVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_letter_id", nullable = false)
    @JsonIgnore
    private CoverLetter coverLetter;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "version_notes")
    private String versionNotes;

    @Column(name = "generated_content", columnDefinition = "TEXT", nullable = false)
    private String generatedContent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "traceability_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> traceability;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
