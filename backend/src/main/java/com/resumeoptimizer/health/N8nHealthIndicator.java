package com.resumeoptimizer.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

@Component("n8nHealthIndicator")
public class N8nHealthIndicator implements HealthIndicator {

    private final String webhookUrl;
    private final RestTemplate restTemplate;

    public N8nHealthIndicator(@Value("${n8n.webhook-url:http://localhost:5678/webhook}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Health health() {
        try {
            // We'll just do a simple GET or OPTIONS to see if the host is reachable
            // Even if the webhook doesn't support GET, reaching the server is a sign it's up
            String baseUrl = webhookUrl.substring(0, webhookUrl.indexOf("/webhook"));
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/healthz", String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return Health.up().withDetail("endpoint", baseUrl).build();
            } else {
                return Health.down().withDetail("status", response.getStatusCode()).build();
            }
        } catch (Exception e) {
            // Fallback: If /healthz doesn't exist, we just note the failure but assume down
            return Health.down().withDetail("endpoint", webhookUrl).withException(e).build();
        }
    }
}
