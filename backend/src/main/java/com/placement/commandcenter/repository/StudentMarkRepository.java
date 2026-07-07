package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.StudentMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentMarkRepository extends JpaRepository<StudentMark, Long> {
    List<StudentMark> findAllByStudentIdOrderBySemesterAsc(Long studentId);
    Optional<StudentMark> findByStudentIdAndSemester(Long studentId, Integer semester);
}
