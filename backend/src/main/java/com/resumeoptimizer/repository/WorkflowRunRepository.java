package com.resumeoptimizer.repository;

import com.resumeoptimizer.entity.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, Long> {
    List<WorkflowRun> findByUserId(Long userId);
    List<WorkflowRun> findByResumeId(Long resumeId);
}
