package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.StudentProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentProjectRepository extends JpaRepository<StudentProject, Long> {
    List<StudentProject> findAllByStudentId(Long studentId);
    Integer countByStudentIdAndStatus(Long studentId, String status);
}
