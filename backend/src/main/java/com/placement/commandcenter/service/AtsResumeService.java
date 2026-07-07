package com.placement.commandcenter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.commandcenter.dto.GenerateResumeRequest;
import com.placement.commandcenter.dto.GeneratedContentDto;
import com.placement.commandcenter.dto.GeneratedResumeResponse;
import com.placement.commandcenter.dto.ResumeListItemResponse;
import com.placement.commandcenter.entity.GeneratedResume;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.TargetCompany;
import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.GeneratedResumeRepository;
import com.placement.commandcenter.repository.StudentRepository;
import com.placement.commandcenter.repository.TargetCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtsResumeService {

    private final StudentRepository studentRepository;
    private final TargetCompanyRepository targetCompanyRepository;
    private final GeneratedResumeRepository resumeRepository;
    private final AiService aiService;
    private final DocumentExportService exportService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public GeneratedResumeResponse generateResume(String email, GenerateResumeRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        if (student.getParsedResumeData() == null) {
            throw new BadRequestException("Please upload and parse your resume in the Skills Inventory tab before using the ATS Tailor Generator.");
        }

        TargetCompany targetCompany = null;
        String jobTitle = "Tailored Resume";
        String companyName = "Target Company";
        String roleName = "Software Engineer";

        if (request.getTargetCompanyId() != null) {
            targetCompany = targetCompanyRepository.findById(request.getTargetCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target company not found"));
            if (!targetCompany.getStudent().getId().equals(student.getId())) {
                throw new BadRequestException("Unauthorized access to target company");
            }
            companyName = targetCompany.getCompanyName();
            roleName = targetCompany.getRole();
            jobTitle = roleName + " at " + companyName;
        }

        String prompt = buildPrompt(student.getName(), student.getParsedResumeData(), companyName, roleName, request.getJobDescription());

        String aiResponse = aiService.generateContent(prompt);
        GeneratedContentDto contentDto = null;
        List<String> matchedKeywords = new ArrayList<>();

        if (aiResponse != null && !aiResponse.trim().isEmpty()) {
            try {
                // Parse AI JSON response
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(aiResponse);
                
                // Construct Content DTO
                contentDto = GeneratedContentDto.builder()
                        .summary(root.path("summary").asText())
                        .skills(objectMapper.convertValue(root.path("skills"), new TypeReference<List<String>>() {}))
                        .experience(objectMapper.convertValue(root.path("experience"), new TypeReference<List<GeneratedContentDto.ExperienceItem>>() {}))
                        .projects(objectMapper.convertValue(root.path("projects"), new TypeReference<List<GeneratedContentDto.ProjectItem>>() {}))
                        .education(objectMapper.convertValue(root.path("education"), new TypeReference<List<GeneratedContentDto.EducationItem>>() {}))
                        .build();

                matchedKeywords = objectMapper.convertValue(root.path("matchedKeywords"), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                System.err.println("Failed to parse AI tailored resume response. Falling back to mock synthesis. Error: " + e.getMessage());
            }
        }

        // Fallback to local mock tailoring if AI fails or is unconfigured
        if (contentDto == null) {
            contentDto = buildMockTailoredContent(student.getParsedResumeData(), companyName, roleName);
            matchedKeywords = Arrays.asList("Java", "Spring Boot", "React", "SQL Tuning", "REST APIs", "Docker");
        }

        try {
            GeneratedResume resume = GeneratedResume.builder()
                    .student(student)
                    .targetCompany(targetCompany)
                    .jobTitle(jobTitle)
                    .generatedContent(objectMapper.writeValueAsString(contentDto))
                    .matchedKeywords(objectMapper.writeValueAsString(matchedKeywords))
                    .isActive(false)
                    .build();

            GeneratedResume saved = resumeRepository.save(resume);
            return mapToResponse(saved);
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize and save generated resume: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<ResumeListItemResponse> getResumes(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return resumeRepository.findAllByStudentIdOrderByCreatedAtDesc(student.getId()).stream()
                .map(r -> ResumeListItemResponse.builder()
                        .id(r.getId())
                        .targetCompanyId(r.getTargetCompany() != null ? r.getTargetCompany().getId() : null)
                        .jobTitle(r.getJobTitle())
                        .createdAt(r.getCreatedAt())
                        .isActive(r.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GeneratedResumeResponse getResume(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        GeneratedResume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!resume.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to resume version");
        }

        return mapToResponse(resume);
    }

    @Transactional
    public void deleteResume(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        GeneratedResume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!resume.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to resume version");
        }

        resumeRepository.delete(resume);
    }

    @Transactional
    public GeneratedResumeResponse activateResume(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        GeneratedResume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!resume.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to resume version");
        }

        // Deactivate previous active one
        Optional<GeneratedResume> activeOpt = resumeRepository.findByStudentIdAndIsActiveTrue(student.getId());
        if (activeOpt.isPresent()) {
            GeneratedResume active = activeOpt.get();
            active.setActive(false);
            resumeRepository.save(active);
        }

        // Activate this one
        resume.setActive(true);
        GeneratedResume saved = resumeRepository.save(resume);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public byte[] downloadResume(String email, Long id, String format) throws IOException {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        GeneratedResume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume version not found with ID: " + id));

        if (!resume.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to resume version");
        }

        GeneratedContentDto content = objectMapper.readValue(resume.getGeneratedContent(), GeneratedContentDto.class);

        if ("pdf".equalsIgnoreCase(format)) {
            return exportService.generatePdf(student.getName(), content);
        } else {
            return exportService.generateDocx(student.getName(), content);
        }
    }

    private String buildPrompt(String studentName, com.placement.commandcenter.dto.ParsedResumeData data, String company, String role, String jd) {
        try {
            return "You are an expert ATS (Applicant Tracking System) resume writer. " +
                    "Optimize the student's resume data for the following target:\n" +
                    "Target Company: " + company + "\n" +
                    "Target Role: " + role + "\n" +
                    "Job Description: " + (jd != null ? jd : "Standard software engineering guidelines") + "\n\n" +
                    "Student Name: " + studentName + "\n" +
                    "Student's Current Resume Data (JSON format):\n" +
                    objectMapper.writeValueAsString(data) + "\n\n" +
                    "Your tasks:\n" +
                    "1. Write a professional ATS summary (maximum 3-4 sentences) tailored to the company, role, and/or job description.\n" +
                    "2. Filter and align the skills list to match key terms from the job target. Do not invent new skills that the student doesn't possess, but prioritize and group the ones they have, highlighting relevant ones.\n" +
                    "3. Rewrite the experience and project bullet points to make them action-oriented, keyword-rich, and tailored to the job target using the Star/XYZ formula (Accomplished [X], as measured by [Y], by doing [Z]).\n" +
                    "4. Identify a list of 5-10 specific keywords from the target/JD that are successfully matched in the optimized resume.\n\n" +
                    "Output format: Respond ONLY with a valid JSON object matching the schema below. No Markdown code fences, no preamble, no explanations. Just raw JSON.\n\n" +
                    "Schema:\n" +
                    "{\n" +
                    "  \"summary\": \"Tailored summary...\",\n" +
                    "  \"skills\": [\"Skill1\", \"Skill2\", ...],\n" +
                    "  \"experience\": [\n" +
                    "    {\n" +
                    "      \"company\": \"Company Name\",\n" +
                    "      \"role\": \"Role Name\",\n" +
                    "      \"duration\": \"Duration\",\n" +
                    "      \"description\": \"Tailored bullet points separated by newlines...\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"projects\": [\n" +
                    "    {\n" +
                    "      \"name\": \"Project Name\",\n" +
                    "      \"description\": \"Tailored description using action verbs...\",\n" +
                    "      \"technologies\": \"React, Java...\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"education\": [\n" +
                    "    {\n" +
                    "      \"school\": \"School Name\",\n" +
                    "      \"degree\": \"Degree Info\",\n" +
                    "      \"year\": \"Year range\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"matchedKeywords\": [\"keyword1\", \"keyword2\", ...]\n" +
                    "}";
        } catch (Exception e) {
            return "Tailor the resume details.";
        }
    }

    private GeneratedContentDto buildMockTailoredContent(com.placement.commandcenter.dto.ParsedResumeData data, String company, String role) {
        // Fallback mockup
        String summary = String.format("Ambitious and detail-oriented computer science student with hands-on project and internship experience, aiming to leverage skills in software development for the %s role at %s. Adept at designing microservices, configuring web systems, and tuning SQL queries to meet team targets.", role, company);

        List<GeneratedContentDto.ExperienceItem> experience = data.getExperience().stream()
                .map(exp -> GeneratedContentDto.ExperienceItem.builder()
                        .company(exp.getCompany())
                        .role(exp.getRole())
                        .duration(exp.getDuration())
                        .description(exp.getDescription() != null 
                                ? exp.getDescription() + "\n* Tailored bullet points for " + company + " role " + role + " utilizing best-practice architectures."
                                : "Assisted development lifecycles.")
                        .build())
                .collect(Collectors.toList());

        List<GeneratedContentDto.ProjectItem> projects = data.getProjects().stream()
                .map(proj -> GeneratedContentDto.ProjectItem.builder()
                        .name(proj.getName())
                        .description(proj.getDescription() != null 
                                ? proj.getDescription() + "\n* Refined features matching target tech requirements."
                                : "Designed technical frameworks.")
                        .technologies(proj.getTechnologies())
                        .build())
                .collect(Collectors.toList());

        List<GeneratedContentDto.EducationItem> education = data.getEducation().stream()
                .map(edu -> GeneratedContentDto.EducationItem.builder()
                        .school(edu.getSchool())
                        .degree(edu.getDegree())
                        .year(edu.getYear())
                        .build())
                .collect(Collectors.toList());

        return GeneratedContentDto.builder()
                .summary(summary)
                .skills(new ArrayList<>(data.getSkills()))
                .experience(experience)
                .projects(projects)
                .education(education)
                .build();
    }

    private GeneratedResumeResponse mapToResponse(GeneratedResume resume) {
        try {
            GeneratedContentDto content = objectMapper.readValue(resume.getGeneratedContent(), GeneratedContentDto.class);
            List<String> keywords = objectMapper.readValue(resume.getMatchedKeywords(), new TypeReference<List<String>>() {});

            return GeneratedResumeResponse.builder()
                    .id(resume.getId())
                    .targetCompanyId(resume.getTargetCompany() != null ? resume.getTargetCompany().getId() : null)
                    .jobTitle(resume.getJobTitle())
                    .generatedContent(content)
                    .matchedKeywords(keywords)
                    .isActive(resume.isActive())
                    .createdAt(resume.getCreatedAt())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to map resume response: " + e.getMessage());
        }
    }
}
