package com.resumeoptimizer.repository;

import com.resumeoptimizer.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByUserId(Long userId);
    java.util.Optional<JobDescription> findByIdAndUserId(Long id, Long userId);
}
