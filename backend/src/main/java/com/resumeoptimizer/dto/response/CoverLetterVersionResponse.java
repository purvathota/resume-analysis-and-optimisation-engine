package com.resumeoptimizer.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class CoverLetterVersionResponse {
    private Long id;
    private Integer versionNumber;
    private String versionNotes;
    private String generatedContent;
    private Map<String, Object> traceability;
    private LocalDateTime createdAt;
}
