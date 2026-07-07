package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.*;
import com.placement.commandcenter.security.UserPrincipal;
import com.placement.commandcenter.service.DsaTaskService;
import com.placement.commandcenter.service.ReadinessScoreEngineService;
import com.placement.commandcenter.service.StudentProfileIntakeService;
import com.placement.commandcenter.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final StudentProfileIntakeService intakeService;
    private final ReadinessScoreEngineService readinessService;
    private final DsaTaskService dsaTaskService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudentProfileResponse profile = studentService.getProfile(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateProfileRequest request) {
        StudentProfileResponse profile = studentService.updateProfile(userPrincipal.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile updated successfully"));
    }

    // Resume Endpoints
    @PostMapping("/me/resume")
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> uploadResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) {
        ResumeUploadResponse response = intakeService.uploadResume(userPrincipal.getEmail(), file);
        return ResponseEntity.ok(ApiResponse.success(response, "Resume uploaded and parsed successfully"));
    }

    @GetMapping("/me/resume")
    public ResponseEntity<ApiResponse<ResumeUploadResponse>> getResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ResumeUploadResponse response = intakeService.getResume(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Resume details retrieved successfully"));
    }

    // Marks Endpoints
    @PostMapping("/me/marks")
    public ResponseEntity<ApiResponse<StudentMarksResponse>> addOrUpdateMark(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SemesterMarkRequest request) {
        StudentMarksResponse response = intakeService.addOrUpdateMark(
                userPrincipal.getEmail(), request.getSemester(), request.getSgpa());
        return ResponseEntity.ok(ApiResponse.success(response, "Marks added/updated successfully"));
    }

    @GetMapping("/me/marks")
    public ResponseEntity<ApiResponse<StudentMarksResponse>> getMarks(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        StudentMarksResponse response = intakeService.getMarks(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Marks details retrieved successfully"));
    }

    // Skills Endpoints
    @PutMapping("/me/skills")
    public ResponseEntity<ApiResponse<SkillsResponse>> updateSkills(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SkillsRequest request) {
        SkillsResponse response = intakeService.updateSkills(userPrincipal.getEmail(), request.getSkills());
        return ResponseEntity.ok(ApiResponse.success(response, "Skills list updated successfully"));
    }

    @GetMapping("/me/skills")
    public ResponseEntity<ApiResponse<SkillsResponse>> getSkills(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SkillsResponse response = intakeService.getSkills(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Skills details retrieved successfully"));
    }

    // Target Companies Endpoints
    @PostMapping("/me/target-companies")
    public ResponseEntity<ApiResponse<TargetCompanyResponse>> addTargetCompany(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TargetCompanyRequest request) {
        TargetCompanyResponse response = intakeService.addTargetCompany(userPrincipal.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Target company added successfully"));
    }

    @GetMapping("/me/target-companies")
    public ResponseEntity<ApiResponse<List<TargetCompanyResponse>>> getTargetCompanies(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TargetCompanyResponse> response = intakeService.getTargetCompanies(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Target companies list retrieved successfully"));
    }

    @DeleteMapping("/me/target-companies/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTargetCompany(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        intakeService.deleteTargetCompany(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Target company deleted successfully"));
    }

    @GetMapping("/me/readiness-score")
    public ResponseEntity<ApiResponse<ReadinessScoreResponse>> getReadinessScore(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ReadinessScoreResponse response = readinessService.getLatestReadinessScore(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Readiness score retrieved successfully"));
    }

    @PostMapping("/me/readiness-score/recalculate")
    public ResponseEntity<ApiResponse<ReadinessScoreResponse>> recalculateReadinessScore(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        ReadinessScoreResponse response = readinessService.recalculateReadinessScore(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Readiness score recalculated successfully"));
    }

    // DSA Practice Endpoints
    @GetMapping("/me/dsa/daily-tasks")
    public ResponseEntity<ApiResponse<DailyTasksResponse>> getDailyTasks(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DailyTasksResponse response = dsaTaskService.getDailyTasks(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Daily DSA tasks retrieved successfully"));
    }

    @PutMapping("/me/dsa/tasks/{problemId}/status")
    public ResponseEntity<ApiResponse<Void>> updateTaskStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long problemId,
            @Valid @RequestBody DsaStatusUpdateRequest request) {
        dsaTaskService.updateTaskStatus(userPrincipal.getEmail(), problemId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null, "DSA problem status updated successfully"));
    }

    @GetMapping("/me/dsa/progress")
    public ResponseEntity<ApiResponse<DsaProgressResponse>> getDsaProgress(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DsaProgressResponse response = dsaTaskService.getProgress(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "DSA progress statistics retrieved successfully"));
    }
}
