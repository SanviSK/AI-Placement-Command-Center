package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.AuthResponse;
import com.placement.commandcenter.dto.LoginRequest;
import com.placement.commandcenter.dto.SignupRequest;
import com.placement.commandcenter.dto.StudentProfileResponse;
import com.placement.commandcenter.entity.Role;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.User;
import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.StudentRepository;
import com.placement.commandcenter.repository.UserRepository;
import com.placement.commandcenter.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public StudentProfileResponse registerStudent(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        // Create user
        User user = User.builder()
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.ROLE_STUDENT)
                .build();

        User savedUser = userRepository.save(user);

        // Create student profile
        Student student = Student.builder()
                .user(savedUser)
                .name(signupRequest.getName())
                .college(signupRequest.getCollege())
                .branch(signupRequest.getBranch())
                .batch(signupRequest.getBatch())
                .skills("")
                .targetCompanies("")
                .marks(0.0)
                .readinessScore(0)
                .build();

        Student savedStudent = studentRepository.save(student);

        return mapToProfileResponse(savedStudent);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        Student student = studentRepository.findByUserEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + loginRequest.getEmail()));

        return AuthResponse.builder()
                .token(token)
                .email(student.getUser().getEmail())
                .name(student.getName())
                .role(student.getUser().getRole().name())
                .build();
    }

    private StudentProfileResponse mapToProfileResponse(Student student) {
        return StudentProfileResponse.builder()
                .id(student.getId())
                .email(student.getUser().getEmail())
                .name(student.getName())
                .college(student.getCollege())
                .branch(student.getBranch())
                .batch(student.getBatch())
                .resumeUrl(student.getResumeUrl())
                .skills(student.getSkills())
                .targetCompanies(student.getTargetCompanies())
                .marks(student.getMarks())
                .readinessScore(student.getReadinessScore())
                .build();
    }
}
