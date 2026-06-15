package com.resumeoptimizer.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoverLetterRequest {
    @NotNull(message = "Resume ID is required")
    private Long resumeId;
    
    private Long jobDescriptionId;
    
    @NotNull(message = "Company Name is required")
    private String companyName;
    
    @NotNull(message = "Role Title is required")
    private String roleTitle;

    private String versionNotes;
}
