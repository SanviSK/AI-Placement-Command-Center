package com.placement.commandcenter.service;

import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.dto.ProjectRecommendationResponse;
import com.placement.commandcenter.dto.StudentProjectRequest;
import com.placement.commandcenter.dto.StudentProjectResponse;
import com.placement.commandcenter.dto.ProjectStatusUpdateRequest;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.StudentProject;
import com.placement.commandcenter.entity.TargetCompany;
import com.placement.commandcenter.exception.ResourceNotFoundException;
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
public class ProjectRecommenderService {

    private final StudentRepository studentRepository;
    private final TargetCompanyRepository targetCompanyRepository;
    private final StudentProjectRepository studentProjectRepository;

    // Hardcoded skill requirement maps per target company (Reused from ReadinessScoreEngineService)
    private static final Map<String, List<String>> COMPANY_SKILLS_MAP = new HashMap<>();

    static {
        COMPANY_SKILLS_MAP.put("google", Arrays.asList("Java", "Go", "Kubernetes", "Algorithms", "System Design"));
        COMPANY_SKILLS_MAP.put("microsoft", Arrays.asList("C#", "TypeScript", "SQL", "Azure", "Data Structures"));
        COMPANY_SKILLS_MAP.put("netflix", Arrays.asList("Java", "React", "AWS", "Microservices", "System Design"));
        COMPANY_SKILLS_MAP.put("amazon", Arrays.asList("Java", "C++", "AWS", "Linux", "Data Structures"));
    }

    private static final List<String> DEFAULT_REQUIRED_SKILLS = Arrays.asList("Java", "React", "PostgreSQL", "Git", "Data Structures");

    // Curated project ideas bank (10 items)
    private static final List<ProjectIdea> PROJECT_IDEA_BANK = Arrays.asList(
            new ProjectIdea(1L, "E-Commerce Microservices Portal", 
                    "Build a scalable e-commerce application using microservices, discovery services, API gateways, and container isolation.",
                    Arrays.asList("Java", "Microservices", "Docker", "PostgreSQL"), "advanced", 6),
            new ProjectIdea(2L, "Cloud-Native API Gateway", 
                    "Implement a lightweight API routing gateway, containerize it, and deploy it to a local Kubernetes cluster with ingress controllers.",
                    Arrays.asList("Go", "Kubernetes", "Docker", "System Design", "AWS"), "advanced", 4),
            new ProjectIdea(3L, "Real-time Chat Engine", 
                    "Build a real-time messaging application with web sockets, presence indicators, message history, and Git source control workflow.",
                    Arrays.asList("TypeScript", "React", "Node.js", "System Design", "Git"), "intermediate", 3),
            new ProjectIdea(4L, "Responsive Job Board Dashboard", 
                    "Develop a job board portal with active search filters, profile managers, database schemas, and clean UI components.",
                    Arrays.asList("React", "TypeScript", "PostgreSQL", "Git", "CSS"), "intermediate", 3),
            new ProjectIdea(5L, "Distributed System Monitoring Tool", 
                    "Create a container daemon agent that runs on Kubernetes clusters, gathers logs, parses statistics, and alerts endpoints.",
                    Arrays.asList("Go", "Kubernetes", "System Design", "AWS", "Git"), "advanced", 5),
            new ProjectIdea(6L, "Enterprise FinTech Platform", 
                    "Build a secure transactional backend ledger application on Azure Cloud using relational structures and algorithm design patterns.",
                    Arrays.asList("C#", "SQL", "Azure", "Data Structures", "Git"), "intermediate", 4),
            new ProjectIdea(7L, "Serverless File Processing Pipeline", 
                    "Build an automated data pipeline on AWS Lambda that processes binary inputs, compiles statistics, and writes report logs.",
                    Arrays.asList("AWS", "Java", "PostgreSQL", "Data Structures", "Git"), "intermediate", 4),
            new ProjectIdea(8L, "Data Structures & Alg Visualizer", 
                    "Develop a visual website explaining tree traversal, sorting behaviors, search operations, and graph layouts in real time.",
                    Arrays.asList("React", "Data Structures", "Algorithms", "Git", "CSS"), "beginner", 2),
            new ProjectIdea(9L, "Local Isolation Sandbox Engine", 
                    "Implement namespaces partitioning, resources control limits, and sandbox filesystem containers on Linux systems using low-level calls.",
                    Arrays.asList("Go", "Linux", "System Design", "Algorithms", "Git"), "advanced", 5),
            new ProjectIdea(10L, "Warehouse Inventory Ledger Service", 
                    "Develop a standard backend warehouse ledger system mapping entity relations, REST APIs, and querying statistics.",
                    Arrays.asList("Java", "SQL", "PostgreSQL", "Git", "Algorithms"), "beginner", 2)
    );

    @Transactional(readOnly = true)
    public List<ProjectRecommendationResponse> getRecommendations(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<TargetCompany> targets = targetCompanyRepository.findAllByStudentId(student.getId());
        List<String> studentSkills = parseSkillsString(student.getSkills());

        // Find missing target skills
        List<String> requiredSkills = getConsolidatedRequiredSkills(targets);
        Set<String> missingSkills = new LinkedHashSet<>();

        for (String reqSkill : requiredSkills) {
            boolean hasSkill = studentSkills.stream()
                    .anyMatch(s -> s.equalsIgnoreCase(reqSkill.trim()));
            if (!hasSkill) {
                missingSkills.add(reqSkill);
            }
        }

        List<ProjectRecommendationResponse> recommendations = new ArrayList<>();

        if (missingSkills.isEmpty()) {
            // General recommendation: top 3 projects from bank
            for (int i = 0; i < Math.min(3, PROJECT_IDEA_BANK.size()); i++) {
                ProjectIdea idea = PROJECT_IDEA_BANK.get(i);
                recommendations.add(ProjectRecommendationResponse.builder()
                        .id(idea.id)
                        .title(idea.title)
                        .description(idea.description)
                        .techStack(idea.techStack)
                        .reasoning("Excellent portfolio project to refine full-stack development skills.")
                        .difficulty(idea.difficulty)
                        .estimatedWeeks(idea.estimatedWeeks)
                        .build());
            }
        } else {
            // Match based on overlap of missing skills
            List<MatchedIdea> matchedIdeas = new ArrayList<>();
            for (ProjectIdea idea : PROJECT_IDEA_BANK) {
                List<String> coveredSkills = idea.techStack.stream()
                        .filter(tech -> missingSkills.stream().anyMatch(ms -> ms.equalsIgnoreCase(tech)))
                        .collect(Collectors.toList());

                if (!coveredSkills.isEmpty()) {
                    matchedIdeas.add(new MatchedIdea(idea, coveredSkills));
                }
            }

            // Sort by overlap size descending
            matchedIdeas.sort((a, b) -> Integer.compare(b.coveredSkills.size(), a.coveredSkills.size()));

            // Take top 3
            List<MatchedIdea> topMatches = matchedIdeas.stream()
                    .limit(3)
                    .collect(Collectors.toList());

            // If we don't have 3, add generic ones from the bank as fallback
            if (topMatches.size() < 3) {
                for (ProjectIdea idea : PROJECT_IDEA_BANK) {
                    if (topMatches.size() >= 3) break;
                    boolean alreadyAdded = topMatches.stream().anyMatch(m -> m.idea.id.equals(idea.id));
                    if (!alreadyAdded) {
                        topMatches.add(new MatchedIdea(idea, new ArrayList<>()));
                    }
                }
            }

            for (MatchedIdea match : topMatches) {
                String reasoning;
                if (!match.coveredSkills.isEmpty()) {
                    reasoning = "Fills critical gap in " + String.join(", ", match.coveredSkills) + " required by your target roles.";
                } else {
                    reasoning = "Strong practical project to diversify your engineering background.";
                }

                recommendations.add(ProjectRecommendationResponse.builder()
                        .id(match.idea.id)
                        .title(match.idea.title)
                        .description(match.idea.description)
                        .techStack(match.idea.techStack)
                        .reasoning(reasoning)
                        .difficulty(match.idea.difficulty)
                        .estimatedWeeks(match.idea.estimatedWeeks)
                        .build());
            }
        }

        return recommendations;
    }

    @Transactional
    public StudentProjectResponse addProject(String email, StudentProjectRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startedAt = null;
        LocalDateTime completedAt = null;

        if ("in-progress".equalsIgnoreCase(request.getStatus()) || "completed".equalsIgnoreCase(request.getStatus())) {
            startedAt = now;
        }
        if ("completed".equalsIgnoreCase(request.getStatus())) {
            completedAt = now;
        }

        StudentProject project = StudentProject.builder()
                .student(student)
                .title(request.getTitle())
                .description(request.getDescription())
                .techStack(request.getTechStack() != null ? String.join(",", request.getTechStack()) : "")
                .status(request.getStatus().toLowerCase())
                .startedAt(startedAt)
                .completedAt(completedAt)
                .sourceRecommendationId(request.getSourceRecommendationId())
                .build();

        StudentProject saved = studentProjectRepository.save(project);
        return mapToProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StudentProjectResponse> getProjects(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        return studentProjectRepository.findAllByStudentId(student.getId()).stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public StudentProjectResponse updateProject(String email, Long id, ProjectStatusUpdateRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        StudentProject project = studentProjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        if (!project.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to project");
        }

        String oldStatus = project.getStatus();
        String newStatus = request.getStatus().toLowerCase();
        project.setStatus(newStatus);

        LocalDateTime now = LocalDateTime.now();
        if ("in-progress".equalsIgnoreCase(newStatus) && project.getStartedAt() == null) {
            project.setStartedAt(now);
        }
        if ("completed".equalsIgnoreCase(newStatus)) {
            if (project.getStartedAt() == null) {
                project.setStartedAt(now);
            }
            project.setCompletedAt(request.getCompletedAt() != null ? request.getCompletedAt() : now);
        } else {
            project.setCompletedAt(null);
        }

        StudentProject saved = studentProjectRepository.save(project);
        return mapToProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        StudentProject project = studentProjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        if (!project.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to project");
        }

        studentProjectRepository.delete(project);
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

    private StudentProjectResponse mapToProjectResponse(StudentProject project) {
        List<String> techList = new ArrayList<>();
        if (StringUtils.hasText(project.getTechStack())) {
            techList = Arrays.stream(project.getTechStack().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return StudentProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .techStack(techList)
                .status(project.getStatus())
                .startedAt(project.getStartedAt())
                .completedAt(project.getCompletedAt())
                .sourceRecommendationId(project.getSourceRecommendationId())
                .build();
    }

    private static class ProjectIdea {
        Long id;
        String title;
        String description;
        List<String> techStack;
        String difficulty;
        int estimatedWeeks;

        ProjectIdea(Long id, String title, String description, List<String> techStack, String difficulty, int estimatedWeeks) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.techStack = techStack;
            this.difficulty = difficulty;
            this.estimatedWeeks = estimatedWeeks;
        }
    }

    private static class MatchedIdea {
        ProjectIdea idea;
        List<String> coveredSkills;

        MatchedIdea(ProjectIdea idea, List<String> coveredSkills) {
            this.idea = idea;
            this.coveredSkills = coveredSkills;
        }
    }
}
