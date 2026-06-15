package com.resumeoptimizer.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeoptimizer.exception.AiServiceException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class AiClientService {

    private static final Logger log = LoggerFactory.getLogger(AiClientService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String apiKey;

    public AiClientService(
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4o}") String model,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper) {

        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @PostConstruct
    public void validateConfiguration() {
        if (apiKey == null || apiKey.isBlank() || "dummy".equals(apiKey)) {
            log.error("╔══════════════════════════════════════════════════════════════╗");
            log.error("║  OPENAI_API_KEY is missing or set to 'dummy'.               ║");
            log.error("║  AI-powered analysis features will NOT work.                 ║");
            log.error("║  Please set OPENAI_API_KEY in your .env file.                ║");
            log.error("╚══════════════════════════════════════════════════════════════╝");
        } else {
            log.info("OpenAI API key configured successfully (model: {})", model);
        }
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !"dummy".equals(apiKey);
    }

    public String generateResponse(String systemPrompt, String userPrompt, boolean jsonMode) {
        if (!isConfigured()) {
            throw new AiServiceException("OpenAI API key is not configured. Please set OPENAI_API_KEY in your .env file.");
        }

        log.info("Initiating OpenAI API request (model: {}, jsonMode: {})", model, jsonMode);
        long startTime = System.currentTimeMillis();

        try {
            Map<String, Object> messageSystem = Map.of(
                    "role", "system",
                    "content", systemPrompt
            );
            Map<String, Object> messageUser = Map.of(
                    "role", "user",
                    "content", userPrompt
            );

            Map<String, Object> requestBody = new java.util.HashMap<>(Map.of(
                    "model", model,
                    "messages", List.of(messageSystem, messageUser),
                    "temperature", 0.0
            ));

            if (jsonMode) {
                requestBody.put("response_format", Map.of("type", "json_object"));
            }

            String responseString = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        int statusCode = response.statusCode().value();
                        return response.bodyToMono(String.class).flatMap(errorBody -> {
                            if (statusCode == 401) {
                                log.error("OpenAI authentication failed — invalid API key");
                                return Mono.error(new AiServiceException(
                                        "OpenAI authentication failed. Please verify your API key is valid."));
                            } else if (statusCode == 429) {
                                log.warn("OpenAI rate limit exceeded or quota exhausted: {}", errorBody);
                                return Mono.error(new AiServiceException("RATE_LIMIT_EXCEEDED"));
                            } else {
                                log.error("OpenAI client error ({}): {}", statusCode, errorBody);
                                return Mono.error(new AiServiceException(
                                        "OpenAI API error (" + statusCode + "): " + errorBody));
                            }
                        });
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("OpenAI server error ({}): {}", response.statusCode().value(), errorBody);
                            return Mono.error(new AiServiceException(
                                    "OpenAI service is temporarily unavailable. Please try again later."));
                        })
                    )
                    .bodyToMono(String.class)
                    .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofSeconds(20))
                            .filter(throwable -> throwable instanceof AiServiceException && "RATE_LIMIT_EXCEEDED".equals(throwable.getMessage()))
                            .doBeforeRetry(retrySignal -> log.info("Rate limit hit. Sleeping and retrying... Attempt {}", retrySignal.totalRetries() + 1)))
                    .onErrorMap(throwable -> {
                        if (throwable instanceof AiServiceException && "RATE_LIMIT_EXCEEDED".equals(throwable.getMessage())) {
                            return new AiServiceException("OpenAI rate limit exceeded. Please wait a minute and try again, or check your OpenAI billing at https://platform.openai.com/account/billing");
                        }
                        return throwable;
                    })
                    .timeout(Duration.ofSeconds(180))
                    .block();

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("OpenAI API response received successfully in {}ms", elapsed);

            JsonNode rootNode = objectMapper.readTree(responseString);
            String content = rootNode.path("choices").get(0).path("message").path("content").asText();

            if (rootNode.has("usage")) {
                JsonNode usage = rootNode.path("usage");
                log.info("OpenAI token usage — prompt: {}, completion: {}, total: {}",
                        usage.path("prompt_tokens").asInt(),
                        usage.path("completion_tokens").asInt(),
                        usage.path("total_tokens").asInt());
            }

            return content;

        } catch (AiServiceException e) {
            throw e; // re-throw already-handled errors
        } catch (WebClientRequestException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Network error connecting to OpenAI after {}ms: {}", elapsed, e.getMessage());
            throw new AiServiceException("Network error connecting to OpenAI. Please check your internet connection.", e);
        } catch (Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                long elapsed = System.currentTimeMillis() - startTime;
                log.error("OpenAI API request timed out after {}ms", elapsed);
                throw new AiServiceException("OpenAI API request timed out after 120 seconds. Please try again.", e);
            }
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("Unexpected error calling OpenAI after {}ms: {}", elapsed, e.getMessage(), e);
            throw new AiServiceException("Failed to call AI service: " + e.getMessage(), e);
        }
    }
}
