package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.*;
import com.placement.commandcenter.entity.DsaProblem;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.StudentDsaProgress;
import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.DsaProblemRepository;
import com.placement.commandcenter.repository.StudentDsaProgressRepository;
import com.placement.commandcenter.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DsaTaskService {

    private final DsaProblemRepository problemRepository;
    private final StudentDsaProgressRepository progressRepository;
    private final StudentRepository studentRepository;

    private static final List<String> DEFAULT_TOPIC_PROGRESSION = Arrays.asList(
            "Arrays", "Strings", "Linked Lists", "Trees", "Graphs", "DP", "Greedy"
    );

    @Transactional
    public DsaProblemResponse addProblem(DsaProblemRequest request) {
        if (problemRepository.existsByTitle(request.getTitle())) {
            throw new BadRequestException("Problem with title '" + request.getTitle() + "' already exists");
        }

        DsaProblem problem = DsaProblem.builder()
                .title(request.getTitle())
                .topic(request.getTopic())
                .difficulty(request.getDifficulty().toUpperCase())
                .leetcodeUrl(request.getLeetcodeUrl())
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : "")
                .build();

        DsaProblem savedProblem = problemRepository.save(problem);
        return mapToProblemResponse(savedProblem);
    }

    @Transactional(readOnly = true)
    public List<DsaProblemResponse> getProblems(String topic, String difficulty) {
        List<DsaProblem> problems;
        if (StringUtils.hasText(topic) && StringUtils.hasText(difficulty)) {
            problems = problemRepository.findAllByTopicAndDifficulty(topic, difficulty.toUpperCase());
        } else if (StringUtils.hasText(topic)) {
            problems = problemRepository.findAllByTopic(topic);
        } else if (StringUtils.hasText(difficulty)) {
            problems = problemRepository.findAllByDifficulty(difficulty.toUpperCase());
        } else {
            problems = problemRepository.findAll();
        }

        return problems.stream()
                .map(this::mapToProblemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DailyTasksResponse getDailyTasks(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        LocalDate today = LocalDate.now();
        List<StudentDsaProgress> todayTasks = progressRepository.findAllByStudentIdAndAssignedDate(student.getId(), today);

        if (!todayTasks.isEmpty()) {
            return mapToDailyTasksResponse(today, todayTasks);
        }

        // Generate new tasks for today
        List<DsaProblem> generatedProblems = generateDailyProblems(student.getId());

        List<StudentDsaProgress> newTasks = new ArrayList<>();
        for (DsaProblem problem : generatedProblems) {
            // Check if there is an existing progress record (e.g. from prior direct solves or previous assignments)
            Optional<StudentDsaProgress> existingProgress = progressRepository
                    .findByStudentIdAndProblemId(student.getId(), problem.getId());

            if (existingProgress.isPresent()) {
                StudentDsaProgress progress = existingProgress.get();
                progress.setAssignedDate(today);
                newTasks.add(progressRepository.save(progress));
            } else {
                StudentDsaProgress progress = StudentDsaProgress.builder()
                        .student(student)
                        .problem(problem)
                        .status("pending")
                        .assignedDate(today)
                        .build();
                newTasks.add(progressRepository.save(progress));
            }
        }

        return mapToDailyTasksResponse(today, newTasks);
    }

    @Transactional
    public void updateTaskStatus(String email, Long problemId, String status) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        DsaProblem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("DSA Problem not found with ID: " + problemId));

        StudentDsaProgress progress = progressRepository
                .findByStudentIdAndProblemId(student.getId(), problemId)
                .orElseGet(() -> StudentDsaProgress.builder()
                        .student(student)
                        .problem(problem)
                        .status("pending")
                        .build());

        String oldStatus = progress.getStatus();
        progress.setStatus(status.toLowerCase());

        if ("solved".equalsIgnoreCase(status)) {
            progress.setSolvedAt(LocalDateTime.now());
            
            // Only update streak if the status transitioned from unsolved to solved
            if (!"solved".equalsIgnoreCase(oldStatus)) {
                updateStreak(student);
            }
        } else {
            progress.setSolvedAt(null);
        }

        progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public DsaProgressResponse getProgress(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<StudentDsaProgress> allProgress = progressRepository.findAllByStudentId(student.getId());
        List<StudentDsaProgress> solvedProgress = allProgress.stream()
                .filter(p -> "solved".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        int totalSolved = solvedProgress.size();

        // 1. Group by Difficulty
        Map<String, Integer> byDifficulty = new HashMap<>();
        byDifficulty.put("easy", 0);
        byDifficulty.put("medium", 0);
        byDifficulty.put("hard", 0);

        for (StudentDsaProgress p : solvedProgress) {
            String diff = p.getProblem().getDifficulty().toLowerCase();
            byDifficulty.put(diff, byDifficulty.getOrDefault(diff, 0) + 1);
        }

        // 2. Group by Topic
        Map<String, Integer> byTopic = new HashMap<>();
        for (String topic : DEFAULT_TOPIC_PROGRESSION) {
            byTopic.put(topic.toLowerCase(), 0);
        }
        for (StudentDsaProgress p : solvedProgress) {
            String topic = p.getProblem().getTopic().toLowerCase();
            byTopic.put(topic, byTopic.getOrDefault(topic, 0) + 1);
        }

        // 3. Solved History for Heatmap
        Map<LocalDate, Integer> historyMap = new TreeMap<>();
        for (StudentDsaProgress p : solvedProgress) {
            if (p.getSolvedAt() != null) {
                LocalDate date = p.getSolvedAt().toLocalDate();
                historyMap.put(date, historyMap.getOrDefault(date, 0) + 1);
            }
        }

        List<DsaProgressResponse.HistoryDto> historyList = historyMap.entrySet().stream()
                .map(entry -> DsaProgressResponse.HistoryDto.builder()
                        .date(entry.getKey())
                        .solvedCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return DsaProgressResponse.builder()
                .totalSolved(totalSolved)
                .byDifficulty(byDifficulty)
                .byTopic(byTopic)
                .currentStreak(student.getCurrentDsaStreak())
                .longestStreak(student.getLongestDsaStreak())
                .history(historyList)
                .build();
    }

    private List<DsaProblem> generateDailyProblems(Long studentId) {
        List<DsaProblem> allProblems = problemRepository.findAll();
        List<StudentDsaProgress> solvedProgress = progressRepository.findAllByStudentId(studentId).stream()
                .filter(p -> "solved".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        Set<Long> solvedProblemIds = solvedProgress.stream()
                .map(p -> p.getProblem().getId())
                .collect(Collectors.toSet());

        // 1. Calculate Weak Topics
        Map<String, List<DsaProblem>> problemsByTopic = allProblems.stream()
                .collect(Collectors.groupingBy(DsaProblem::getTopic));

        Map<String, Long> solvedByTopic = solvedProgress.stream()
                .collect(Collectors.groupingBy(p -> p.getProblem().getTopic(), Collectors.counting()));

        // Solve ratio per topic = solved / total available
        List<TopicSolveRatio> ratios = new ArrayList<>();
        for (String topic : DEFAULT_TOPIC_PROGRESSION) {
            List<DsaProblem> topicProblems = problemsByTopic.getOrDefault(topic, new ArrayList<>());
            long total = topicProblems.size();
            long solved = solvedByTopic.getOrDefault(topic, 0L);
            double ratio = total > 0 ? (double) solved / total : 1.0;
            ratios.add(new TopicSolveRatio(topic, ratio));
        }

        // Sort ascending by ratio (lowest ratio = weakest topic)
        ratios.sort(Comparator.comparingDouble(t -> t.ratio));

        List<String> weakTopics = ratios.stream()
                .map(r -> r.topic)
                .collect(Collectors.toList());

        // 2. Select 3-5 Problems (We aim for 3: 2 Easy/Medium + 1 Hard)
        List<DsaProblem> selected = new ArrayList<>();

        // Group unsolved problems by topic and difficulty
        List<DsaProblem> unsolvedProblems = allProblems.stream()
                .filter(p -> !solvedProblemIds.contains(p.getId()))
                .collect(Collectors.toList());

        // Pick Easy/Medium problems from weak topics first
        List<DsaProblem> easyMediumUnsolved = unsolvedProblems.stream()
                .filter(p -> "EASY".equalsIgnoreCase(p.getDifficulty()) || "MEDIUM".equalsIgnoreCase(p.getDifficulty()))
                .collect(Collectors.toList());

        List<DsaProblem> hardUnsolved = unsolvedProblems.stream()
                .filter(p -> "HARD".equalsIgnoreCase(p.getDifficulty()))
                .collect(Collectors.toList());

        // Select 2 Easy/Medium
        for (String topic : weakTopics) {
            if (selected.size() >= 2) break;
            List<DsaProblem> topicUnsolved = easyMediumUnsolved.stream()
                    .filter(p -> p.getTopic().equalsIgnoreCase(topic))
                    .collect(Collectors.toList());
            for (DsaProblem p : topicUnsolved) {
                if (selected.size() < 2) {
                    selected.add(p);
                } else {
                    break;
                }
            }
        }

        // Fallback for Easy/Medium if not enough in weak topics
        if (selected.size() < 2) {
            for (DsaProblem p : easyMediumUnsolved) {
                if (!selected.contains(p) && selected.size() < 2) {
                    selected.add(p);
                }
            }
        }

        // Select 1 Hard
        for (String topic : weakTopics) {
            if (selected.size() >= 3) break;
            List<DsaProblem> topicHard = hardUnsolved.stream()
                    .filter(p -> p.getTopic().equalsIgnoreCase(topic))
                    .collect(Collectors.toList());
            if (!topicHard.isEmpty()) {
                selected.add(topicHard.get(0));
                break;
            }
        }

        // Fallback for Hard if not found in weak topics
        if (selected.size() < 3 && !hardUnsolved.isEmpty()) {
            selected.add(hardUnsolved.get(0));
        }

        // Final fallback: if selected size is still 0, add any unsolved problems
        if (selected.isEmpty()) {
            for (DsaProblem p : unsolvedProblems) {
                if (selected.size() < 3) {
                    selected.add(p);
                }
            }
        }

        // If student solved literally everything, add random duplicate problems to keep tasks active
        if (selected.isEmpty()) {
            Collections.shuffle(allProblems);
            selected = allProblems.subList(0, Math.min(3, allProblems.size()));
        }

        return selected;
    }

    private void updateStreak(Student student) {
        LocalDate today = LocalDate.now();
        LocalDate lastSolved = student.getLastDsaSolvedDate();

        if (lastSolved == null) {
            student.setCurrentDsaStreak(1);
            student.setLongestDsaStreak(Math.max(1, student.getLongestDsaStreak()));
        } else if (lastSolved.equals(today)) {
            // Already solved today, keep current streak
        } else if (lastSolved.equals(today.minusDays(1))) {
            student.setCurrentDsaStreak(student.getCurrentDsaStreak() + 1);
            student.setLongestDsaStreak(Math.max(student.getCurrentDsaStreak(), student.getLongestDsaStreak()));
        } else {
            student.setCurrentDsaStreak(1);
            student.setLongestDsaStreak(Math.max(1, student.getLongestDsaStreak()));
        }

        student.setLastDsaSolvedDate(today);
        studentRepository.save(student);
    }

    private DsaProblemResponse mapToProblemResponse(DsaProblem problem) {
        List<String> tagList = new ArrayList<>();
        if (StringUtils.hasText(problem.getTags())) {
            tagList = Arrays.stream(problem.getTags().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        return DsaProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .topic(problem.getTopic())
                .difficulty(problem.getDifficulty())
                .leetcodeUrl(problem.getLeetcodeUrl())
                .tags(tagList)
                .build();
    }

    private DailyTasksResponse mapToDailyTasksResponse(LocalDate date, List<StudentDsaProgress> progressList) {
        List<DailyTasksResponse.TaskDto> tasks = progressList.stream()
                .map(p -> DailyTasksResponse.TaskDto.builder()
                        .problemId(p.getProblem().getId())
                        .title(p.getProblem().getTitle())
                        .topic(p.getProblem().getTopic())
                        .difficulty(p.getProblem().getDifficulty())
                        .leetcodeUrl(p.getProblem().getLeetcodeUrl())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());

        return DailyTasksResponse.builder()
                .date(date)
                .tasks(tasks)
                .build();
    }

    private static class TopicSolveRatio {
        String topic;
        double ratio;

        TopicSolveRatio(String topic, double ratio) {
            this.topic = topic;
            this.ratio = ratio;
        }
    }
}
