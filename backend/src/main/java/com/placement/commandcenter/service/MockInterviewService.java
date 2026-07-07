package com.placement.commandcenter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.placement.commandcenter.dto.*;
import com.placement.commandcenter.entity.InterviewQuestion;
import com.placement.commandcenter.entity.InterviewSession;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.entity.TargetCompany;
import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.InterviewQuestionRepository;
import com.placement.commandcenter.repository.InterviewSessionRepository;
import com.placement.commandcenter.repository.StudentRepository;
import com.placement.commandcenter.repository.TargetCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MockInterviewService {

    private final StudentRepository studentRepository;
    private final TargetCompanyRepository targetCompanyRepository;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewQuestionRepository questionRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int TOTAL_QUESTIONS = 5;

    @Transactional
    public StartInterviewResponse startInterview(String email, StartInterviewRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        TargetCompany targetCompany = null;
        String companyName = "General Company";
        String roleName = "Software Engineer";

        if (request.getTargetCompanyId() != null) {
            targetCompany = targetCompanyRepository.findById(request.getTargetCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target company not found"));
            if (!targetCompany.getStudent().getId().equals(student.getId())) {
                throw new BadRequestException("Unauthorized access to target company");
            }
            companyName = targetCompany.getCompanyName();
            roleName = targetCompany.getRole();
        }

        // Create session
        InterviewSession session = InterviewSession.builder()
                .student(student)
                .interviewType(request.getInterviewType())
                .targetCompany(targetCompany)
                .build();
        InterviewSession savedSession = sessionRepository.save(session);

        // Generate first question
        String firstQuestionText = generateFirstQuestionText(student, request.getInterviewType(), companyName, roleName);

        // Save first question
        InterviewQuestion firstQuestion = InterviewQuestion.builder()
                .session(savedSession)
                .questionText(firstQuestionText)
                .questionOrder(1)
                .build();
        InterviewQuestion savedQuestion = questionRepository.save(firstQuestion);

        return StartInterviewResponse.builder()
                .sessionId(savedSession.getId())
                .question(QuestionDto.builder()
                        .id(savedQuestion.getId())
                        .text(savedQuestion.getQuestionText())
                        .build())
                .questionNumber(1)
                .totalQuestions(TOTAL_QUESTIONS)
                .build();
    }

    @Transactional
    public SubmitAnswerResponse submitAnswer(String email, Long sessionId, SubmitAnswerRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));

        if (!session.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to interview session");
        }

        InterviewQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (!question.getSession().getId().equals(session.getId())) {
            throw new BadRequestException("Question does not belong to this interview session");
        }

        if (question.getAnswerText() != null) {
            throw new BadRequestException("Answer already submitted for this question");
        }

        // 1. Evaluate Current Answer
        FeedbackDto feedback = evaluateAnswer(question.getQuestionText(), request.getAnswerText(), session.getInterviewType());

        // Update current question
        try {
            question.setAnswerText(request.getAnswerText());
            question.setScore(feedback.getScore());
            question.setStrengths(objectMapper.writeValueAsString(feedback.getStrengths()));
            question.setImprovements(objectMapper.writeValueAsString(feedback.getImprovements()));
            questionRepository.save(question);
        } catch (Exception e) {
            throw new BadRequestException("Failed to serialize feedback: " + e.getMessage());
        }

        QuestionDto nextQuestionDto = null;
        int currentOrder = question.getQuestionOrder();

        if (currentOrder < TOTAL_QUESTIONS) {
            // 2. Generate Next Question
            List<InterviewQuestion> previousQuestions = questionRepository.findAllBySessionIdOrderByQuestionOrderAsc(sessionId);
            String nextQuestionText = generateNextQuestionText(student, session.getInterviewType(), previousQuestions);

            // Save next question
            InterviewQuestion nextQuestion = InterviewQuestion.builder()
                    .session(session)
                    .questionText(nextQuestionText)
                    .questionOrder(currentOrder + 1)
                    .build();
            InterviewQuestion savedNext = questionRepository.save(nextQuestion);

            nextQuestionDto = QuestionDto.builder()
                    .id(savedNext.getId())
                    .text(savedNext.getQuestionText())
                    .build();
        } else {
            // Complete Interview Session
            List<InterviewQuestion> allQuestions = questionRepository.findAllBySessionIdOrderByQuestionOrderAsc(sessionId);
            double totalScore = 0.0;
            for (InterviewQuestion q : allQuestions) {
                totalScore += (q.getScore() != null ? q.getScore() : 0.0);
            }
            double average = totalScore / allQuestions.size();

            session.setOverallScore(average);
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }

        return SubmitAnswerResponse.builder()
                .feedback(feedback)
                .nextQuestion(nextQuestionDto)
                .questionNumber(currentOrder)
                .totalQuestions(TOTAL_QUESTIONS)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InterviewListItemResponse> getInterviews(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<InterviewSession> sessions = sessionRepository.findAllByStudentIdOrderByCreatedAtDesc(student.getId());

        return sessions.stream()
                .map(s -> {
                    List<InterviewQuestion> qs = questionRepository.findAllBySessionIdOrderByQuestionOrderAsc(s.getId());
                    int answeredCount = (int) qs.stream().filter(q -> q.getAnswerText() != null).count();
                    
                    return InterviewListItemResponse.builder()
                            .sessionId(s.getId())
                            .interviewType(s.getInterviewType())
                            .targetCompanyId(s.getTargetCompany() != null ? s.getTargetCompany().getId() : null)
                            .companyName(s.getTargetCompany() != null ? s.getTargetCompany().getCompanyName() : "General")
                            .role(s.getTargetCompany() != null ? s.getTargetCompany().getRole() : "Software Engineer")
                            .overallScore(s.getOverallScore())
                            .completedAt(s.getCompletedAt())
                            .questionCount(answeredCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InterviewTranscriptResponse getInterviewTranscript(String email, Long sessionId) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));

        if (!session.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to interview session");
        }

        List<InterviewQuestion> questions = questionRepository.findAllBySessionIdOrderByQuestionOrderAsc(sessionId);

        List<InterviewTranscriptResponse.TranscriptQuestionDto> qs = questions.stream()
                .map(q -> {
                    FeedbackDto feedback = null;
                    if (q.getScore() != null) {
                        try {
                            List<String> strengths = objectMapper.readValue(q.getStrengths(), new TypeReference<List<String>>() {});
                            List<String> improvements = objectMapper.readValue(q.getImprovements(), new TypeReference<List<String>>() {});
                            feedback = FeedbackDto.builder()
                                    .score(q.getScore())
                                    .strengths(strengths)
                                    .improvements(improvements)
                                    .build();
                        } catch (Exception e) {
                            feedback = FeedbackDto.builder().score(q.getScore()).build();
                        }
                    }

                    return InterviewTranscriptResponse.TranscriptQuestionDto.builder()
                            .questionText(q.getQuestionText())
                            .answerText(q.getAnswerText())
                            .feedback(feedback)
                            .build();
                })
                .collect(Collectors.toList());

        return InterviewTranscriptResponse.builder()
                .sessionId(session.getId())
                .interviewType(session.getInterviewType())
                .targetCompanyId(session.getTargetCompany() != null ? session.getTargetCompany().getId() : null)
                .companyName(session.getTargetCompany() != null ? session.getTargetCompany().getCompanyName() : "General")
                .role(session.getTargetCompany() != null ? session.getTargetCompany().getRole() : "Software Engineer")
                .overallScore(session.getOverallScore())
                .completedAt(session.getCompletedAt())
                .questions(qs)
                .build();
    }

    @Transactional
    public void deleteInterview(String email, Long sessionId) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));

        if (!session.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to interview session");
        }

        sessionRepository.delete(session);
    }

    private String generateFirstQuestionText(Student student, String type, String company, String role) {
        String prompt = String.format(
                "You are an expert technical recruiter conducting a mock interview.\n" +
                "Candidate details:\n" +
                "- Name: %s\n" +
                "- College: %s\n" +
                "- Skills: %s\n" +
                "Target Company: %s\n" +
                "Target Role: %s\n" +
                "Interview Type: %s\n\n" +
                "Generate the FIRST question for this interview session. Make it relevant to their target company, role, and the interview type.\n" +
                "Respond with a JSON object format matching:\n" +
                "{ \"question\": \"Write the question text here...\" }\n" +
                "Do not write any other text or markdown code fences.",
                student.getName(), student.getCollege(), student.getSkills(), company, role, type
        );

        String jsonResponse = aiService.generateContent(prompt);
        if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
                return root.path("question").asText();
            } catch (Exception e) {
                System.err.println("Failed to parse first question JSON: " + e.getMessage());
            }
        }

        // Fallback mockup
        if ("technical".equalsIgnoreCase(type)) {
            return String.format("Welcome! Let's start with a technical question. Tell me about a challenging backend or frontend issue you faced in your project and how you went about solving it, specifically detailing your tech stack.", role, company);
        } else if ("behavioral".equalsIgnoreCase(type)) {
            return "Tell me about a time you had to deal with a conflict in a team setting. What was the situation and how did you resolve it?";
        } else {
            return String.format("Why are you interested in joining %s for the %s role?", company, role);
        }
    }

    private String generateNextQuestionText(Student student, String type, List<InterviewQuestion> history) {
        StringBuilder builder = new StringBuilder();
        for (InterviewQuestion q : history) {
            builder.append("Q: ").append(q.getQuestionText()).append("\n");
            builder.append("A: ").append(q.getAnswerText() != null ? q.getAnswerText() : "(No Answer)").append("\n\n");
        }

        String prompt = String.format(
                "You are an interviewer conducting a mock interview session for a candidate (Skills: %s).\n" +
                "Interview Type: %s\n" +
                "Session History:\n%s" +
                "Based on the conversation history above, generate the NEXT relevant question (number %d of 5).\n" +
                "You can choose to follow up on their previous answer or transition to a new relevant topic.\n" +
                "Respond with a JSON object format matching:\n" +
                "{ \"question\": \"Write the next question text here...\" }\n" +
                "Do not write any other text or markdown code fences.",
                student.getSkills(), type, builder.toString(), history.size() + 1
        );

        String jsonResponse = aiService.generateContent(prompt);
        if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
                return root.path("question").asText();
            } catch (Exception e) {
                System.err.println("Failed to parse next question JSON: " + e.getMessage());
            }
        }

        // Fallback mockup
        int order = history.size() + 1;
        if ("technical".equalsIgnoreCase(type)) {
            return String.format("For Question %d: How would you design a scalable system or explain a time-complexity tradeoff you made recently?", order);
        } else if ("behavioral".equalsIgnoreCase(type)) {
            return String.format("For Question %d: Tell me about a time you failed to meet a deadline. What did you do?", order);
        } else {
            return String.format("For Question %d: What are your greatest strengths and how do they match the requirements?", order);
        }
    }

    private FeedbackDto evaluateAnswer(String question, String answer, String type) {
        String prompt = String.format(
                "You are an interview scorer. Rate and evaluate this answer:\n" +
                "Question: %s\n" +
                "Answer: %s\n" +
                "Interview Type: %s\n\n" +
                "Provide a rating score from 0 to 10 (integer), a list of strengths (bullet points), and a list of areas for improvement.\n" +
                "Respond with a JSON object format matching:\n" +
                "{\n" +
                "  \"score\": 8,\n" +
                "  \"strengths\": [\"Strength point 1\", \"Strength point 2\"],\n" +
                "  \"improvements\": [\"Improvement point 1\", \"Improvement point 2\"]\n" +
                "}\n" +
                "Do not write any other text or markdown code fences.",
                question, answer, type
        );

        String jsonResponse = aiService.generateContent(prompt);
        if (jsonResponse != null && !jsonResponse.trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(jsonResponse);
                return FeedbackDto.builder()
                        .score(root.path("score").asInt(7))
                        .strengths(objectMapper.convertValue(root.path("strengths"), new TypeReference<List<String>>() {}))
                        .improvements(objectMapper.convertValue(root.path("improvements"), new TypeReference<List<String>>() {}))
                        .build();
            } catch (Exception e) {
                System.err.println("Failed to parse answer feedback JSON: " + e.getMessage());
            }
        }

        // Fallback mockup
        return FeedbackDto.builder()
                .score(8)
                .strengths(Arrays.asList("Clear articulation of basic terminology", "Linked answer to practical project experiences"))
                .improvements(Arrays.asList("Provide concrete performance metrics", "Detail error handling scenarios"))
                .build();
    }
}
