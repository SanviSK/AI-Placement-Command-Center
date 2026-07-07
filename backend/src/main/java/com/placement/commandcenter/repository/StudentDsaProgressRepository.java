package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.StudentDsaProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDsaProgressRepository extends JpaRepository<StudentDsaProgress, Long> {
    List<StudentDsaProgress> findAllByStudentId(Long studentId);
    Optional<StudentDsaProgress> findByStudentIdAndProblemId(Long studentId, Long problemId);
    List<StudentDsaProgress> findAllByStudentIdAndAssignedDate(Long studentId, LocalDate assignedDate);
    Integer countByStudentIdAndStatus(Long studentId, String status);
}
