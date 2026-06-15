package com.resumeoptimizer.service.workflow;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class WorkflowService {

    private final WebClient webClient;
    
    @Value("${n8n.webhook-url}")
    private String n8nWebhookUrl;

    public WorkflowService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public void triggerOptimizationWorkflow(Long resumeId, Long jobDescriptionId, String candidateEmail) {
        Map<String, Object> payload = Map.of(
            "resumeId", resumeId,
            "jobDescriptionId", jobDescriptionId,
            "candidateEmail", candidateEmail,
            "action", "START_OPTIMIZATION"
        );

        try {
            webClient.post()
                .uri(n8nWebhookUrl)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
                
            System.out.println("Successfully triggered n8n workflow for resume ID: " + resumeId);
        } catch (Exception e) {
            System.err.println("Failed to trigger n8n workflow: " + e.getMessage());
            // Depending on requirements, we might want to throw an exception here
            // or implement a retry mechanism.
        }
    }
}
