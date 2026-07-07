package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.ApplicationStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationStageHistoryRepository extends JpaRepository<ApplicationStageHistory, Long> {
    List<ApplicationStageHistory> findAllByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
