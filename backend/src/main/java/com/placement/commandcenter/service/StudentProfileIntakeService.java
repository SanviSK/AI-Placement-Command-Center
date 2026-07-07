package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.*;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.StudentMark;
import com.placement.commandcenter.entity.TargetCompany;
import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.StudentMarkRepository;
import com.placement.commandcenter.repository.StudentRepository;
import com.placement.commandcenter.repository.TargetCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentProfileIntakeService {

    private final StudentRepository studentRepository;
    private final StudentMarkRepository studentMarkRepository;
    private final TargetCompanyRepository targetCompanyRepository;
    private final ResumeParserService resumeParserService;

    private static final String UPLOAD_DIR = "uploads";

    @Transactional
    public ResumeUploadResponse uploadResume(String email, MultipartFile file) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty");
        }

        // 1. Create upload directory if it does not exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create uploads directory", e);
        }

        // 2. Generate unique filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "resume.pdf";
        }
        String filename = "student_" + student.getId() + "_" + System.currentTimeMillis() + "_" + originalFilename;
        Path targetPath = Paths.get(UPLOAD_DIR).resolve(filename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store resume file locally", e);
        }

        // 3. Extract text and parse with AI / Mock
        String extractedText = resumeParserService.extractText(file);
        ParsedResumeData parsedData = resumeParserService.parseResume(extractedText);

        // 4. Update student profile URL and JSON data
        student.setResumeUrl("/uploads/" + filename);
        student.setParsedResumeData(parsedData);

        // 5. Merge parsed skills into student's existing skills list
        if (parsedData.getSkills() != null && !parsedData.getSkills().isEmpty()) {
            Set<String> currentSkillsSet = new LinkedHashSet<>();
            if (StringUtils.hasText(student.getSkills())) {
                Arrays.stream(student.getSkills().split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .forEach(currentSkillsSet::add);
            }
            parsedData.getSkills().forEach(skill -> currentSkillsSet.add(skill.trim()));
            student.setSkills(String.join(", ", currentSkillsSet));
        }

        // 6. Recalculate readiness score
        student.setReadinessScore(calculateReadinessScore(student));

        studentRepository.save(student);

        return ResumeUploadResponse.builder()
                .resumeUrl(student.getResumeUrl())
                .parsedData(parsedData)
                .build();
    }

    @Transactional(readOnly = true)
    public ResumeUploadResponse getResume(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return ResumeUploadResponse.builder()
                .resumeUrl(student.getResumeUrl())
                .parsedData(student.getParsedResumeData())
                .build();
    }

    @Transactional
    public StudentMarksResponse addOrUpdateMark(String email, Integer semester, Double sgpa) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        if (semester < 1 || semester > 8) {
            throw new BadRequestException("Semester must be between 1 and 8");
        }
        if (sgpa < 0.0 || sgpa > 10.0) {
            throw new BadRequestException("SGPA must be between 0.0 and 10.0");
        }

        Optional<StudentMark> existingMarkOpt = studentMarkRepository.findByStudentIdAndSemester(student.getId(), semester);

        if (existingMarkOpt.isPresent()) {
            StudentMark existingMark = existingMarkOpt.get();
            existingMark.setSgpa(sgpa);
            studentMarkRepository.save(existingMark);
        } else {
            StudentMark newMark = StudentMark.builder()
                    .student(student)
                    .semester(semester)
                    .sgpa(sgpa)
                    .build();
            studentMarkRepository.save(newMark);
        }

        // Compute overall CGPA
        List<StudentMark> allMarks = studentMarkRepository.findAllByStudentIdOrderBySemesterAsc(student.getId());
        double overallCgpa = 0.0;
        if (!allMarks.isEmpty()) {
            double sum = allMarks.stream().mapToDouble(StudentMark::getSgpa).sum();
            overallCgpa = sum / allMarks.size();
        }

        // Round overall CGPA to 2 decimal places
        overallCgpa = Math.round(overallCgpa * 100.0) / 100.0;

        // Save back into student profile
        student.setMarks(overallCgpa);
        student.setReadinessScore(calculateReadinessScore(student));
        studentRepository.save(student);

        return mapToMarksResponse(allMarks, overallCgpa);
    }

    @Transactional(readOnly = true)
    public StudentMarksResponse getMarks(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<StudentMark> allMarks = studentMarkRepository.findAllByStudentIdOrderBySemesterAsc(student.getId());
        double overallCgpa = student.getMarks() != null ? student.getMarks() : 0.0;

        return mapToMarksResponse(allMarks, overallCgpa);
    }

    @Transactional
    public SkillsResponse updateSkills(String email, List<String> skills) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        if (skills == null) {
            skills = new ArrayList<>();
        }

        // Clean list and join with commas
        String skillsString = skills.stream()
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(", "));

        student.setSkills(skillsString);
        student.setReadinessScore(calculateReadinessScore(student));
        studentRepository.save(student);

        return SkillsResponse.builder()
                .skills(parseSkillsString(skillsString))
                .build();
    }

    @Transactional(readOnly = true)
    public SkillsResponse getSkills(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return SkillsResponse.builder()
                .skills(parseSkillsString(student.getSkills()))
                .build();
    }

    @Transactional
    public TargetCompanyResponse addTargetCompany(String email, TargetCompanyRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        TargetCompany company = TargetCompany.builder()
                .student(student)
                .companyName(request.getCompanyName())
                .role(request.getRole())
                .packageBand(request.getPackageBand())
                .applicationDeadline(request.getApplicationDeadline())
                .build();

        TargetCompany savedCompany = targetCompanyRepository.save(company);

        // Update student readiness score (as company list count has changed)
        student.setReadinessScore(calculateReadinessScore(student));
        studentRepository.save(student);

        return mapToCompanyResponse(savedCompany);
    }

    @Transactional(readOnly = true)
    public List<TargetCompanyResponse> getTargetCompanies(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<TargetCompany> companies = targetCompanyRepository.findAllByStudentId(student.getId());
        return companies.stream()
                .map(this::mapToCompanyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTargetCompany(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        TargetCompany company = targetCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Target company not found with ID: " + id));

        if (!company.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to delete this target company");
        }

        targetCompanyRepository.delete(company);

        // Update student readiness score (as company list count has changed)
        student.setReadinessScore(calculateReadinessScore(student));
        studentRepository.save(student);
    }

    private int calculateReadinessScore(Student student) {
        int score = 0;
        
        // 20 pts: basic profile fields filled
        if (StringUtils.hasText(student.getName()) && 
            StringUtils.hasText(student.getCollege()) && 
            StringUtils.hasText(student.getBranch()) && 
            StringUtils.hasText(student.getBatch())) {
            score += 20;
        }

        // 20 pts: marks entered
        if (student.getMarks() != null && student.getMarks() > 0) {
            score += 20;
        }

        // 20 pts: skills populated
        if (StringUtils.hasText(student.getSkills())) {
            String[] skillsArray = student.getSkills().split(",");
            score += Math.min(20, skillsArray.length * 5); // 5 points per skill up to 20
        }

        // 20 pts: target companies selected
        List<TargetCompany> targetCompaniesList = targetCompanyRepository.findAllByStudentId(student.getId());
        if (!targetCompaniesList.isEmpty()) {
            score += 20;
        }

        // 20 pts: resume parsed
        if (StringUtils.hasText(student.getResumeUrl())) {
            score += 20;
        }

        return score;
    }

    private List<String> parseSkillsString(String skillsStr) {
        if (!StringUtils.hasText(skillsStr)) {
            return new ArrayList<>();
        }
        return Arrays.stream(skillsStr.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private StudentMarksResponse mapToMarksResponse(List<StudentMark> marks, double overallCgpa) {
        List<StudentMarksResponse.SemesterMarkDto> list = marks.stream()
                .map(m -> StudentMarksResponse.SemesterMarkDto.builder()
                        .id(m.getId())
                        .semester(m.getSemester())
                        .sgpa(m.getSgpa())
                        .build())
                .collect(Collectors.toList());

        return StudentMarksResponse.builder()
                .marks(list)
                .overallCgpa(overallCgpa)
                .build();
    }

    private TargetCompanyResponse mapToCompanyResponse(TargetCompany company) {
        return TargetCompanyResponse.builder()
                .id(company.getId())
                .companyName(company.getCompanyName())
                .role(company.getRole())
                .packageBand(company.getPackageBand())
                .applicationDeadline(company.getApplicationDeadline())
                .build();
    }
}
