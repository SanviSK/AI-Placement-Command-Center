package com.placement.commandcenter.controller;

import com.placement.commandcenter.common.ApiResponse;
import com.placement.commandcenter.dto.AuthResponse;
import com.placement.commandcenter.dto.LoginRequest;
import com.placement.commandcenter.dto.SignupRequest;
import com.placement.commandcenter.dto.StudentProfileResponse;
import com.placement.commandcenter.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<StudentProfileResponse>> signup(@Valid @RequestBody SignupRequest signupRequest) {
        StudentProfileResponse profile = authService.registerStudent(signupRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(profile, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity
                .ok(ApiResponse.success(response, "Login successful"));
    }
}
