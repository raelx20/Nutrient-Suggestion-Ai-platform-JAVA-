package com.vitaledge.web.controller;

import com.vitaledge.security.SecurityUtils;
import com.vitaledge.service.AssessmentService;
import com.vitaledge.web.dto.assessment.AnswerRequest;
import com.vitaledge.web.dto.assessment.AnswerResponse;
import com.vitaledge.web.dto.assessment.AssessmentDetailResponse;
import com.vitaledge.web.dto.assessment.StartAssessmentResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/start")
    public ResponseEntity<StartAssessmentResponse> start() {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(assessmentService.start(userId));
    }

    @PostMapping("/{assessmentId}/answers")
    public ResponseEntity<AnswerResponse.BatchResult> submitAnswers(
            @PathVariable UUID assessmentId,
            @Valid @RequestBody AnswerRequest.Batch batch) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(assessmentService.submitAnswers(userId, assessmentId, batch.answers()));
    }

    @PostMapping("/{assessmentId}/complete")
    public ResponseEntity<AssessmentDetailResponse> complete(@PathVariable UUID assessmentId) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(assessmentService.complete(userId, assessmentId));
    }

    @GetMapping("/{assessmentId}")
    public ResponseEntity<AssessmentDetailResponse> get(@PathVariable UUID assessmentId) {
        UUID userId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(assessmentService.get(userId, assessmentId));
    }
}