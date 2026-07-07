package com.placement.commandcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "student_dsa_progress",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "problem_id"})}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDsaProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private DsaProblem problem;

    @Column(nullable = false)
    private String status; // pending, solved

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    @Column(name = "assigned_date")
    private LocalDate assignedDate;
}
