package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.ParsedResumeData;
import com.placement.commandcenter.dto.ReadinessScoreResponse;
import com.placement.commandcenter.entity.ReadinessScoreHistory;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.TargetCompany;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.ReadinessScoreHistoryRepository;
import com.placement.commandcenter.repository.StudentDsaProgressRepository;
import com.placement.commandcenter.repository.StudentProjectRepository;
import com.placement.commandcenter.repository.StudentRepository;
import com.placement.commandcenter.repository.TargetCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadinessScoreEngineService {

    private final StudentRepository studentRepository;
    private final TargetCompanyRepository targetCompanyRepository;
    private final ReadinessScoreHistoryRepository readinessScoreHistoryRepository;
    private final StudentDsaProgressRepository studentDsaProgressRepository;
    private final StudentProjectRepository studentProjectRepository;

    // Hardcoded skill requirement maps per target company
    private static final Map<String, List<String>> COMPANY_SKILLS_MAP = new HashMap<>();

    static {
        COMPANY_SKILLS_MAP.put("google", Arrays.asList("Java", "Go", "Kubernetes", "Algorithms", "System Design"));
        COMPANY_SKILLS_MAP.put("microsoft", Arrays.asList("C#", "TypeScript", "SQL", "Azure", "Data Structures"));
        COMPANY_SKILLS_MAP.put("netflix", Arrays.asList("Java", "React", "AWS", "Microservices", "System Design"));
        COMPANY_SKILLS_MAP.put("amazon", Arrays.asList("Java", "C++", "AWS", "Linux", "Data Structures"));
    }

    private static final List<String> DEFAULT_REQUIRED_SKILLS = Arrays.asList("Java", "React", "PostgreSQL", "Git", "Data Structures");

    @Transactional
    public ReadinessScoreResponse getLatestReadinessScore(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Optional<ReadinessScoreHistory> latestOpt = readinessScoreHistoryRepository
                .findFirstByStudentIdOrderByCalculatedAtDesc(student.getId());

        if (latestOpt.isPresent()) {
            ReadinessScoreHistory history = latestOpt.get();
            return ReadinessScoreResponse.builder()
                    .overallScore(history.getScore())
                    .breakdown(history.getBreakdown())
                    .lastCalculatedAt(history.getCalculatedAt())
                    .build();
        }

        // If no score has been calculated yet, calculate it now
        return calculateAndSaveReadiness(student);
    }

    @Transactional
    public ReadinessScoreResponse recalculateReadinessScore(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return calculateAndSaveReadiness(student);
    }

    private ReadinessScoreResponse calculateAndSaveReadiness(Student student) {
        List<TargetCompany> targets = targetCompanyRepository.findAllByStudentId(student.getId());
        List<String> studentSkills = parseSkillsString(student.getSkills());
        ParsedResumeData resumeData = student.getParsedResumeData();

        // 1. Academics (CGPA-based): 20%
        double cgpa = student.getMarks() != null ? student.getMarks() : 0.0;
        int academicsScore = (int) Math.min(100, Math.max(0, (cgpa / 10.0) * 100));
        ReadinessScoreResponse.AcademicsDetail academics = ReadinessScoreResponse.AcademicsDetail.builder()
                .score(academicsScore)
                .weight(0.20)
                .cgpa(cgpa)
                .build();

        // 2. DSA Progress: 25% (Based on real solved counts)
        int problemsSolved = studentDsaProgressRepository.countByStudentIdAndStatus(student.getId(), "solved");
        int dsaScore = 0;
        if (problemsSolved >= 26) {
            dsaScore = 100;
        } else if (problemsSolved >= 16) {
            dsaScore = 85;
        } else if (problemsSolved >= 6) {
            dsaScore = 60;
        } else if (problemsSolved >= 1) {
            dsaScore = 30;
        }
        
        ReadinessScoreResponse.DsaDetail dsaProgress = ReadinessScoreResponse.DsaDetail.builder()
                .score(dsaScore)
                .weight(0.25)
                .problemsSolved(problemsSolved)
                .build();

        // 3. Projects: 15% (Count completed projects from database)
        int projectCount = studentProjectRepository.countByStudentIdAndStatus(student.getId(), "completed");
        int projectsScore = 0;
        if (projectCount == 1) {
            projectsScore = 50;
        } else if (projectCount >= 2) {
            projectsScore = 100;
        }
        ReadinessScoreResponse.ProjectsDetail projects = ReadinessScoreResponse.ProjectsDetail.builder()
                .score(projectsScore)
                .weight(0.15)
                .count(projectCount)
                .build();

        // 4. Skill Match vs Target Companies: 25%
        List<String> requiredSkills = getConsolidatedRequiredSkills(targets);
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String reqSkill : requiredSkills) {
            boolean hasSkill = studentSkills.stream()
                    .anyMatch(s -> s.equalsIgnoreCase(reqSkill.trim()));
            if (hasSkill) {
                matchedSkills.add(reqSkill);
            } else {
                missingSkills.add(reqSkill);
            }
        }

        int skillMatchScore = 0;
        if (!requiredSkills.isEmpty()) {
            skillMatchScore = (int) Math.round(((double) matchedSkills.size() / requiredSkills.size()) * 100);
        }
        ReadinessScoreResponse.SkillMatchDetail skillMatch = ReadinessScoreResponse.SkillMatchDetail.builder()
                .score(skillMatchScore)
                .weight(0.25)
                .matchedSkills(matchedSkills)
                .missingSkills(missingSkills)
                .build();

        // 5. Resume Completeness: 15%
        List<String> missingFields = new ArrayList<>();
        int resumeCompletenessScore = 0;
        if (StringUtils.hasText(student.getResumeUrl())) {
            if (resumeData != null) {
                if (resumeData.getSkills() != null && !resumeData.getSkills().isEmpty()) resumeCompletenessScore += 20;
                else missingFields.add("skills");

                if (resumeData.getEducation() != null && !resumeData.getEducation().isEmpty()) resumeCompletenessScore += 20;
                else missingFields.add("education");

                if (resumeData.getProjects() != null && !resumeData.getProjects().isEmpty()) resumeCompletenessScore += 20;
                else missingFields.add("projects");

                if (resumeData.getExperience() != null && !resumeData.getExperience().isEmpty()) resumeCompletenessScore += 20;
                else missingFields.add("experience");

                if (resumeData.getCertifications() != null && !resumeData.getCertifications().isEmpty()) resumeCompletenessScore += 20;
                else missingFields.add("certifications");
            } else {
                missingFields.addAll(Arrays.asList("skills", "education", "projects", "experience", "certifications"));
            }
        } else {
            resumeCompletenessScore = 0;
            missingFields.addAll(Arrays.asList("resume_file", "skills", "education", "projects", "experience", "certifications"));
        }
        ReadinessScoreResponse.ResumeCompletenessDetail resumeCompleteness = ReadinessScoreResponse.ResumeCompletenessDetail.builder()
                .score(resumeCompletenessScore)
                .weight(0.15)
                .missingFields(missingFields)
                .build();

        // Compute Overall Weighted Score
        double overallWeighted = (academicsScore * 0.20) + 
                                 (dsaProgress.getScore() * 0.25) + 
                                 (projectsScore * 0.15) + 
                                 (skillMatchScore * 0.25) + 
                                 (resumeCompletenessScore * 0.15);

        int overallScore = (int) Math.round(overallWeighted);

        // Update cached score in student table
        student.setReadinessScore(overallScore);
        studentRepository.save(student);

        // Save entry to historical database
        ReadinessScoreResponse.Breakdown breakdown = ReadinessScoreResponse.Breakdown.builder()
                .academics(academics)
                .dsaProgress(dsaProgress)
                .projects(projects)
                .skillMatch(skillMatch)
                .resumeCompleteness(resumeCompleteness)
                .build();

        LocalDateTime calculatedTime = LocalDateTime.now();

        ReadinessScoreHistory history = ReadinessScoreHistory.builder()
                .student(student)
                .score(overallScore)
                .breakdown(breakdown)
                .calculatedAt(calculatedTime)
                .build();

        readinessScoreHistoryRepository.save(history);

        return ReadinessScoreResponse.builder()
                .overallScore(overallScore)
                .breakdown(breakdown)
                .lastCalculatedAt(calculatedTime)
                .build();
    }

    private List<String> getConsolidatedRequiredSkills(List<TargetCompany> targets) {
        if (targets == null || targets.isEmpty()) {
            return DEFAULT_REQUIRED_SKILLS;
        }

        Set<String> requiredSet = new LinkedHashSet<>();
        for (TargetCompany company : targets) {
            String companyKey = company.getCompanyName().toLowerCase().trim();
            List<String> companySkills = COMPANY_SKILLS_MAP.get(companyKey);
            if (companySkills != null) {
                requiredSet.addAll(companySkills);
            } else {
                // If company name not in map, suggest general web/programming defaults
                requiredSet.addAll(DEFAULT_REQUIRED_SKILLS);
            }
        }
        return new ArrayList<>(requiredSet);
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
}
