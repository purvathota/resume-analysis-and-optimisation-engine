package com.resumeoptimizer.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMetrics {

    private final Counter resumeGenerations;
    private final Counter coverLetterGenerations;
    private final Counter jobApplicationCreations;

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.resumeGenerations = Counter.builder("resume.generations.count")
                .description("Number of resume optimizations performed")
                .register(meterRegistry);

        this.coverLetterGenerations = Counter.builder("coverletter.generations.count")
                .description("Number of cover letters generated")
                .register(meterRegistry);

        this.jobApplicationCreations = Counter.builder("jobapplication.creations.count")
                .description("Number of job applications tracked")
                .register(meterRegistry);
    }

    public void incrementResumeGeneration() {
        resumeGenerations.increment();
    }

    public void incrementCoverLetterGeneration() {
        coverLetterGenerations.increment();
    }

    public void incrementJobApplicationCreation() {
        jobApplicationCreations.increment();
    }
}
