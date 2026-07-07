package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.DsaProblemRequest;
import com.placement.commandcenter.dto.DsaProblemResponse;
import com.placement.commandcenter.service.DsaTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dsa")
@RequiredArgsConstructor
public class DsaController {

    private final DsaTaskService dsaTaskService;

    @PostMapping("/problems")
    public ResponseEntity<ApiResponse<DsaProblemResponse>> addProblem(
            @Valid @RequestBody DsaProblemRequest request) {
        DsaProblemResponse response = dsaTaskService.addProblem(request);
        return ResponseEntity.ok(ApiResponse.success(response, "DSA problem added successfully"));
    }

    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<List<DsaProblemResponse>>> getProblems(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String difficulty) {
        List<DsaProblemResponse> response = dsaTaskService.getProblems(topic, difficulty);
        return ResponseEntity.ok(ApiResponse.success(response, "DSA problems retrieved successfully"));
    }
}
