package com.resumeoptimizer.repository;

import com.resumeoptimizer.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    Optional<Analysis> findByResumeIdAndJobDescriptionId(Long resumeId, Long jobDescriptionId);
    List<Analysis> findByResumeUserId(Long userId);
}
