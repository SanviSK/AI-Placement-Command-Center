package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.StudentProfileResponse;
import com.placement.commandcenter.dto.UpdateProfileRequest;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + email));
        return mapToProfileResponse(student);
    }

    @Transactional
    public StudentProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for email: " + email));

        if (request.getName() != null) student.setName(request.getName());
        if (request.getCollege() != null) student.setCollege(request.getCollege());
        if (request.getBranch() != null) student.setBranch(request.getBranch());
        if (request.getBatch() != null) student.setBatch(request.getBatch());
        if (request.getSkills() != null) student.setSkills(request.getSkills());
        if (request.getTargetCompanies() != null) student.setTargetCompanies(request.getTargetCompanies());
        if (request.getMarks() != null) student.setMarks(request.getMarks());

        // Dynamic Readiness Score heuristic calculation
        int score = calculateReadinessScore(student);
        student.setReadinessScore(score);

        Student updatedStudent = studentRepository.save(student);
        return mapToProfileResponse(updatedStudent);
    }

    private int calculateReadinessScore(Student student) {
        int score = 0;
        // 20 points for basic fields completed
        if (StringUtils.hasText(student.getName()) && 
            StringUtils.hasText(student.getCollege()) && 
            StringUtils.hasText(student.getBranch()) && 
            StringUtils.hasText(student.getBatch())) {
            score += 20;
        }
        
        // 20 points for academic performance (marks > 0)
        if (student.getMarks() != null && student.getMarks() > 0) {
            score += 20;
        }

        // 20 points for skills listing
        if (StringUtils.hasText(student.getSkills())) {
            String[] skillsList = student.getSkills().split(",");
            score += Math.min(20, skillsList.length * 5); // 5 points per skill up to 20
        }

        // 20 points for target companies
        if (StringUtils.hasText(student.getTargetCompanies())) {
            score += 20;
        }

        // 20 points for resume uploading (mocked as 0 for now until S3 is integrated)
        if (StringUtils.hasText(student.getResumeUrl())) {
            score += 20;
        }

        return score;
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
