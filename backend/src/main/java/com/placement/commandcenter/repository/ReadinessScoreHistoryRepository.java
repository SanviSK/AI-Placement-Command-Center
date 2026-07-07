package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.ReadinessScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadinessScoreHistoryRepository extends JpaRepository<ReadinessScoreHistory, Long> {
    Optional<ReadinessScoreHistory> findFirstByStudentIdOrderByCalculatedAtDesc(Long studentId);
    List<ReadinessScoreHistory> findAllByStudentIdOrderByCalculatedAtAsc(Long studentId);
}
