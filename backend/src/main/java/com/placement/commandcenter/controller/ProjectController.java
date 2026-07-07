package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.ProjectRecommendationResponse;
import com.placement.commandcenter.dto.StudentProjectRequest;
import com.placement.commandcenter.dto.StudentProjectResponse;
import com.placement.commandcenter.dto.ProjectStatusUpdateRequest;
import com.placement.commandcenter.security.UserPrincipal;
import com.placement.commandcenter.service.ProjectRecommenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectRecommenderService projectService;

    @GetMapping("/me/projects/recommendations")
    public ResponseEntity<ApiResponse<List<ProjectRecommendationResponse>>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ProjectRecommendationResponse> response = projectService.getRecommendations(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Project recommendations compiled successfully"));
    }

    @PostMapping("/me/projects")
    public ResponseEntity<ApiResponse<StudentProjectResponse>> addProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody StudentProjectRequest request) {
        StudentProjectResponse response = projectService.addProject(userPrincipal.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Project added to portfolio successfully"));
    }

    @GetMapping("/me/projects")
    public ResponseEntity<ApiResponse<List<StudentProjectResponse>>> getProjects(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<StudentProjectResponse> response = projectService.getProjects(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Student projects retrieved successfully"));
    }

    @PutMapping("/me/projects/{id}")
    public ResponseEntity<ApiResponse<StudentProjectResponse>> updateProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody ProjectStatusUpdateRequest request) {
        StudentProjectResponse response = projectService.updateProject(userPrincipal.getEmail(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Project updated successfully"));
    }

    @DeleteMapping("/me/projects/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        projectService.deleteProject(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Project deleted successfully"));
    }
}
