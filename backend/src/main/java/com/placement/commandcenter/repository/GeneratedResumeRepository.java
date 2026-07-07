package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.GeneratedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedResumeRepository extends JpaRepository<GeneratedResume, Long> {
    List<GeneratedResume> findAllByStudentIdOrderByCreatedAtDesc(Long studentId);
    Optional<GeneratedResume> findByStudentIdAndIsActiveTrue(Long studentId);
}
