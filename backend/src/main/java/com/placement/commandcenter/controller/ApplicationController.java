package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.*;
import com.placement.commandcenter.security.UserPrincipal;
import com.placement.commandcenter.service.ApplicationTrackerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationTrackerService applicationService;

    @PostMapping("/me/applications")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.createApplication(userPrincipal.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Application tracked successfully"));
    }

    @GetMapping("/me/applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String stage) {
        List<ApplicationResponse> response = applicationService.getApplications(userPrincipal.getEmail(), stage);
        return ResponseEntity.ok(ApiResponse.success(response, "Applications retrieved successfully"));
    }

    @PutMapping("/me/applications/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationDetails(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = applicationService.updateApplicationDetails(userPrincipal.getEmail(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Application details updated successfully"));
    }

    @PutMapping("/me/applications/{id}/stage")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStageUpdateRequest request) {
        ApplicationResponse response = applicationService.updateApplicationStage(userPrincipal.getEmail(), id, request.getStage());
        return ResponseEntity.ok(ApiResponse.success(response, "Application stage updated successfully"));
    }

    @DeleteMapping("/me/applications/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteApplication(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        applicationService.deleteApplication(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Application deleted successfully"));
    }

    @GetMapping("/me/applications/{id}/history")
    public ResponseEntity<ApiResponse<List<StageHistoryResponse>>> getStageHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        List<StageHistoryResponse> response = applicationService.getStageHistory(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Application stage history retrieved successfully"));
    }

    @GetMapping("/me/applications/upcoming-deadlines")
    public ResponseEntity<ApiResponse<List<UpcomingDeadlineResponse>>> getUpcomingDeadlines(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<UpcomingDeadlineResponse> response = applicationService.getUpcomingDeadlines(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Upcoming deadlines retrieved successfully"));
    }
}
