package com.placement.commandcenter.entity;

import com.placement.commandcenter.dto.ReadinessScoreResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "readiness_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessScoreHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Integer score;

    @Convert(converter = ReadinessScoreBreakdownConverter.class)
    @Column(columnDefinition = "TEXT")
    private ReadinessScoreResponse.Breakdown breakdown;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
