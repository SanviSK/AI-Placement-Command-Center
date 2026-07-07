package com.placement.commandcenter.service;

import com.placement.commandcenter.dto.ApplicationRequest;
import com.placement.commandcenter.dto.ApplicationResponse;
import com.placement.commandcenter.dto.StageHistoryResponse;
import com.placement.commandcenter.dto.UpcomingDeadlineResponse;
import com.placement.commandcenter.entity.Application;
import com.placement.commandcenter.entity.ApplicationStageHistory;
import com.placement.commandcenter.entity.Student;
import com.placement.commandcenter.exception.BadRequestException;
import com.placement.commandcenter.exception.ResourceNotFoundException;
import com.placement.commandcenter.repository.ApplicationRepository;
import com.placement.commandcenter.repository.ApplicationStageHistoryRepository;
import com.placement.commandcenter.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationTrackerService {

    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationStageHistoryRepository historyRepository;

    @Transactional
    public ApplicationResponse createApplication(String email, ApplicationRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Application application = Application.builder()
                .student(student)
                .companyName(request.getCompanyName())
                .role(request.getRole())
                .appliedDate(request.getAppliedDate())
                .stage("Applied")
                .packageBand(request.getPackageBand())
                .jobUrl(request.getJobUrl())
                .notes(request.getNotes())
                .reminderDate(request.getReminderDate())
                .build();

        Application saved = applicationRepository.save(application);

        // Log initial stage history
        ApplicationStageHistory history = ApplicationStageHistory.builder()
                .application(saved)
                .stage("Applied")
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);

        return mapToApplicationResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplications(String email, String stage) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        List<Application> apps;
        if (StringUtils.hasText(stage)) {
            apps = applicationRepository.findAllByStudentIdAndStage(student.getId(), stage);
        } else {
            apps = applicationRepository.findAllByStudentId(student.getId());
        }

        return apps.stream()
                .map(this::mapToApplicationResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateApplicationDetails(String email, Long id, ApplicationRequest request) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to application");
        }

        application.setCompanyName(request.getCompanyName());
        application.setRole(request.getRole());
        application.setAppliedDate(request.getAppliedDate());
        application.setPackageBand(request.getPackageBand());
        application.setJobUrl(request.getJobUrl());
        application.setNotes(request.getNotes());
        application.setReminderDate(request.getReminderDate());

        Application saved = applicationRepository.save(application);
        return mapToApplicationResponse(saved);
    }

    @Transactional
    public ApplicationResponse updateApplicationStage(String email, Long id, String newStage) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to application");
        }

        String oldStage = application.getStage();
        if (oldStage.equalsIgnoreCase(newStage)) {
            return mapToApplicationResponse(application);
        }

        application.setStage(newStage);
        Application saved = applicationRepository.save(application);

        // Log stage update
        ApplicationStageHistory history = ApplicationStageHistory.builder()
                .application(saved)
                .stage(newStage)
                .changedAt(LocalDateTime.now())
                .build();
        historyRepository.save(history);

        return mapToApplicationResponse(saved);
    }

    @Transactional
    public void deleteApplication(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to application");
        }

        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public List<StageHistoryResponse> getStageHistory(String email, Long id) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        if (!application.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("Unauthorized access to application");
        }

        return historyRepository.findAllByApplicationIdOrderByChangedAtAsc(id).stream()
                .map(h -> StageHistoryResponse.builder()
                        .stage(h.getStage())
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UpcomingDeadlineResponse> getUpcomingDeadlines(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        LocalDate today = LocalDate.now();
        List<Application> apps = applicationRepository
                .findAllByStudentIdAndReminderDateGreaterThanEqualOrderByReminderDateAsc(student.getId(), today);

        return apps.stream()
                .map(app -> UpcomingDeadlineResponse.builder()
                        .id(app.getId())
                        .companyName(app.getCompanyName())
                        .role(app.getRole())
                        .stage(app.getStage())
                        .reminderDate(app.getReminderDate())
                        .daysRemaining(ChronoUnit.DAYS.between(today, app.getReminderDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private ApplicationResponse mapToApplicationResponse(Application app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .companyName(app.getCompanyName())
                .role(app.getRole())
                .appliedDate(app.getAppliedDate())
                .stage(app.getStage())
                .packageBand(app.getPackageBand())
                .jobUrl(app.getJobUrl())
                .notes(app.getNotes())
                .reminderDate(app.getReminderDate())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
