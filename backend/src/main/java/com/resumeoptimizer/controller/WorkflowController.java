package com.resumeoptimizer.controller;

import com.resumeoptimizer.security.CustomUserDetails;
import com.resumeoptimizer.service.workflow.WorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerWorkflow(
            @RequestParam Long resumeId,
            @RequestParam Long jobDescriptionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
            
        workflowService.triggerOptimizationWorkflow(resumeId, jobDescriptionId, userDetails.getUsername());
        
        return ResponseEntity.ok("Workflow triggered successfully. It will run in the background.");
    }
}
