package com.resumeoptimizer.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@Component("openAiHealthIndicator")
public class OpenAiHealthIndicator implements HealthIndicator {

    private final String apiKey;
    private final RestTemplate restTemplate;

    public OpenAiHealthIndicator(@Value("${openai.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Health health() {
        if (apiKey == null || apiKey.isEmpty() || "dummy".equals(apiKey)) {
            return Health.unknown().withDetail("status", "API key not configured").build();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/models",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return Health.up().withDetail("endpoint", "api.openai.com").build();
            } else {
                return Health.down().withDetail("status", response.getStatusCode()).build();
            }
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
