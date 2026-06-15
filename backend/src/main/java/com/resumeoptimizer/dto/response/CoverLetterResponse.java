package com.resumeoptimizer.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CoverLetterResponse {
    private Long id;
    private String companyName;
    private String roleTitle;
    private List<CoverLetterVersionResponse> versions;
    private LocalDateTime createdAt;
}
