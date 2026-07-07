package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.*;
import com.placement.commandcenter.security.UserPrincipal;
import com.placement.commandcenter.service.MockInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/me/interviews")
@RequiredArgsConstructor
public class MockInterviewController {

    private final MockInterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartInterviewResponse>> startInterview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StartInterviewRequest request) {
        StartInterviewResponse response = interviewService.startInterview(userPrincipal.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview session started successfully"));
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<ApiResponse<SubmitAnswerResponse>> submitAnswer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        SubmitAnswerResponse response = interviewService.submitAnswer(userPrincipal.getEmail(), sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Answer submitted and evaluated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InterviewListItemResponse>>> getInterviews(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<InterviewListItemResponse> response = interviewService.getInterviews(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Interview sessions retrieved successfully"));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<InterviewTranscriptResponse>> getInterviewTranscript(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long sessionId) {
        InterviewTranscriptResponse response = interviewService.getInterviewTranscript(userPrincipal.getEmail(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview transcript retrieved successfully"));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long sessionId) {
        interviewService.deleteInterview(userPrincipal.getEmail(), sessionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Interview session deleted successfully"));
    }
}
