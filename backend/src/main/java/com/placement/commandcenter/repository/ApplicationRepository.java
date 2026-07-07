package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findAllByStudentId(Long studentId);
    List<Application> findAllByStudentIdAndStage(Long studentId, String stage);
    List<Application> findAllByStudentIdAndReminderDateGreaterThanEqualOrderByReminderDateAsc(Long studentId, LocalDate date);
}
