package com.resumeoptimizer.dto.response;

import com.resumeoptimizer.entity.ApplicationStatus;
import com.resumeoptimizer.entity.CompanyType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class JobApplicationResponse {
    private Long id;
    private String companyName;
    private String roleTitle;
    private String location;
    private ApplicationStatus status;
    private CompanyType companyType;
    private String url;
    private String notes;
    private LocalDate appliedDate;

    // Optional lightweight details about linked documents
    private Long resumeId;
    private String resumeFileName;
    
    private Long coverLetterId;
    private Integer coverLetterVersionNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
