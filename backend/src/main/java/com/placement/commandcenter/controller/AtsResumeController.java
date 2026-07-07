package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.GenerateResumeRequest;
import com.placement.commandcenter.dto.GeneratedResumeResponse;
import com.placement.commandcenter.dto.ResumeListItemResponse;
import com.placement.commandcenter.security.UserPrincipal;
import com.placement.commandcenter.service.AtsResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/students/me/resumes")
@RequiredArgsConstructor
public class AtsResumeController {

    private final AtsResumeService resumeService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<GeneratedResumeResponse>> generateResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody GenerateResumeRequest request) {
        GeneratedResumeResponse response = resumeService.generateResume(userPrincipal.getEmail(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tailored ATS Resume generated successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResumeListItemResponse>>> getResumes(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<ResumeListItemResponse> response = resumeService.getResumes(userPrincipal.getEmail());
        return ResponseEntity.ok(ApiResponse.success(response, "Generated resume versions retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GeneratedResumeResponse>> getResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        GeneratedResumeResponse response = resumeService.getResume(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Resume details retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        resumeService.deleteResume(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "Resume version deleted successfully"));
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<GeneratedResumeResponse>> activateResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        GeneratedResumeResponse response = resumeService.activateResume(userPrincipal.getEmail(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "Resume version set to Active successfully"));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam(defaultValue = "pdf") String format) throws IOException {
        byte[] data = resumeService.downloadResume(userPrincipal.getEmail(), id, format);
        
        HttpHeaders headers = new HttpHeaders();
        if ("pdf".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ats_tailored_resume.pdf");
        } else {
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
            headers.setContentDispositionFormData("attachment", "ats_tailored_resume.docx");
        }
        
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
