package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.DsaProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DsaProblemRepository extends JpaRepository<DsaProblem, Long> {
    Boolean existsByTitle(String title);
    List<DsaProblem> findAllByTopic(String topic);
    List<DsaProblem> findAllByDifficulty(String difficulty);
    List<DsaProblem> findAllByTopicAndDifficulty(String topic, String difficulty);
}
