package com.resumeoptimizer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResponse {
    private Long id;
    private String fileName;
    private String message;
}
