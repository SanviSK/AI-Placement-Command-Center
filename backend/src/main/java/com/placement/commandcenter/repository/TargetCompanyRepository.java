package com.placement.commandcenter.repository;

import com.placement.commandcenter.entity.TargetCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TargetCompanyRepository extends JpaRepository<TargetCompany, Long> {
    List<TargetCompany> findAllByStudentId(Long studentId);
}
