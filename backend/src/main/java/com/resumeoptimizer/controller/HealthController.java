package com.resumeoptimizer.controller;

import com.resumeoptimizer.service.ai.AiClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final AiClientService aiClientService;

    public HealthController(JdbcTemplate jdbcTemplate, AiClientService aiClientService) {
        this.jdbcTemplate = jdbcTemplate;
        this.aiClientService = aiClientService;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");

        try {
            jdbcTemplate.execute("SELECT 1");
            healthStatus.put("database", "CONNECTED");
        } catch (Exception e) {
            healthStatus.put("database", "DISCONNECTED");
        }

        if (aiClientService.isConfigured()) {
            healthStatus.put("openai", "CONFIGURED");
        } else {
            healthStatus.put("openai", "NOT_CONFIGURED");
        }

        return ResponseEntity.ok(healthStatus);
    }
}
