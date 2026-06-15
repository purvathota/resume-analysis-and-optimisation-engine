package com.resumeoptimizer.dto.request;

import com.resumeoptimizer.entity.ApplicationStatus;
import com.resumeoptimizer.entity.CompanyType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class JobApplicationRequest {
    @NotNull(message = "Company name is required")
    private String companyName;

    @NotNull(message = "Role title is required")
    private String roleTitle;

    private String location;

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    private CompanyType companyType;
    private String url;
    private String notes;
    private LocalDate appliedDate;

    private Long resumeId;
    private Long coverLetterId;
}
