package com.resumeoptimizer.service.jobtracker;

import com.resumeoptimizer.dto.request.JobApplicationRequest;
import com.resumeoptimizer.dto.response.JobApplicationResponse;
import com.resumeoptimizer.entity.ApplicationStatus;
import com.resumeoptimizer.entity.CoverLetter;
import com.resumeoptimizer.entity.JobApplication;
import com.resumeoptimizer.entity.Resume;
import com.resumeoptimizer.entity.User;
import com.resumeoptimizer.repository.CoverLetterRepository;
import com.resumeoptimizer.repository.JobApplicationRepository;
import com.resumeoptimizer.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final ResumeRepository resumeRepository;
    private final CoverLetterRepository coverLetterRepository;

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getUserApplications(Long userId) {
        return jobApplicationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobApplicationResponse createApplication(User user, JobApplicationRequest request) {
        JobApplication application = JobApplication.builder()
                .user(user)
                .companyName(request.getCompanyName())
                .roleTitle(request.getRoleTitle())
                .location(request.getLocation())
                .status(request.getStatus())
                .companyType(request.getCompanyType())
                .url(request.getUrl())
                .notes(request.getNotes())
                .appliedDate(request.getAppliedDate())
                .build();

        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findByIdAndUserId(request.getResumeId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Resume not found"));
            application.setResume(resume);
        }

        if (request.getCoverLetterId() != null) {
            CoverLetter coverLetter = coverLetterRepository.findByIdAndUserId(request.getCoverLetterId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Cover letter not found"));
            application.setCoverLetter(coverLetter);
        }

        application = jobApplicationRepository.save(application);
        return mapToResponse(application);
    }

    @Transactional
    public JobApplicationResponse updateApplication(Long id, User user, JobApplicationRequest request) {
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Job application not found"));

        application.setCompanyName(request.getCompanyName());
        application.setRoleTitle(request.getRoleTitle());
        application.setLocation(request.getLocation());
        application.setStatus(request.getStatus());
        application.setCompanyType(request.getCompanyType());
        application.setUrl(request.getUrl());
        application.setNotes(request.getNotes());
        application.setAppliedDate(request.getAppliedDate());

        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findByIdAndUserId(request.getResumeId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Resume not found"));
            application.setResume(resume);
        } else {
            application.setResume(null);
        }

        if (request.getCoverLetterId() != null) {
            CoverLetter coverLetter = coverLetterRepository.findByIdAndUserId(request.getCoverLetterId(), user.getId())
                    .orElseThrow(() -> new RuntimeException("Cover letter not found"));
            application.setCoverLetter(coverLetter);
        } else {
            application.setCoverLetter(null);
        }

        application = jobApplicationRepository.save(application);
        return mapToResponse(application);
    }

    @Transactional
    public JobApplicationResponse updateStatus(Long id, User user, ApplicationStatus newStatus) {
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Job application not found"));
        application.setStatus(newStatus);
        application = jobApplicationRepository.save(application);
        return mapToResponse(application);
    }

    @Transactional
    public void deleteApplication(Long id, User user) {
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Job application not found"));
        jobApplicationRepository.delete(application);
    }

    private JobApplicationResponse mapToResponse(JobApplication application) {
        JobApplicationResponse.JobApplicationResponseBuilder builder = JobApplicationResponse.builder()
                .id(application.getId())
                .companyName(application.getCompanyName())
                .roleTitle(application.getRoleTitle())
                .location(application.getLocation())
                .status(application.getStatus())
                .companyType(application.getCompanyType())
                .url(application.getUrl())
                .notes(application.getNotes())
                .appliedDate(application.getAppliedDate())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt());

        if (application.getResume() != null) {
            builder.resumeId(application.getResume().getId());
            builder.resumeFileName(application.getResume().getFileName());
        }

        if (application.getCoverLetter() != null) {
            builder.coverLetterId(application.getCoverLetter().getId());
            // Map the latest version number if versions exist
            if (application.getCoverLetter().getVersions() != null && !application.getCoverLetter().getVersions().isEmpty()) {
                int latestVersion = application.getCoverLetter().getVersions().stream()
                        .mapToInt(com.resumeoptimizer.entity.CoverLetterVersion::getVersionNumber)
                        .max().orElse(1);
                builder.coverLetterVersionNumber(latestVersion);
            }
        }

        return builder.build();
    }
}
