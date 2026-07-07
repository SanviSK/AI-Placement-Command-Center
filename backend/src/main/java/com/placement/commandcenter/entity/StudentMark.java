package com.placement.commandcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "student_marks",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "semester"})}
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false)
    private Double sgpa;
}
