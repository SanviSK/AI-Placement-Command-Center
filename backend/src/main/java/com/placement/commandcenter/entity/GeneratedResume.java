package com.placement.commandcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "generated_resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_company_id")
    private TargetCompany targetCompany;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "generated_content", columnDefinition = "TEXT", nullable = false)
    private String generatedContent; // Stores JSON content: summary, skills[], experience[], projects[], education[]

    @Column(name = "matched_keywords", columnDefinition = "TEXT")
    private String matchedKeywords; // Stores JSON array of strings

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
