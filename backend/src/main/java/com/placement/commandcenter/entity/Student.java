package com.placement.commandcenter.entity;

import com.placement.commandcenter.dto.ParsedResumeData;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String college;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private String batch;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(columnDefinition = "TEXT")
    private String skills; // Comma-separated skills

    @Column(name = "target_companies", columnDefinition = "TEXT")
    private String targetCompanies; // Comma-separated target companies

    private Double marks;

    @Column(name = "readiness_score")
    @Builder.Default
    private Integer readinessScore = 0;

    @Convert(converter = ParsedResumeDataConverter.class)
    @Column(name = "parsed_resume_data", columnDefinition = "TEXT")
    private ParsedResumeData parsedResumeData;

    @Column(name = "current_dsa_streak")
    @Builder.Default
    private Integer currentDsaStreak = 0;

    @Column(name = "longest_dsa_streak")
    @Builder.Default
    private Integer longestDsaStreak = 0;

    @Column(name = "last_dsa_solved_date")
    private java.time.LocalDate lastDsaSolvedDate;
}
