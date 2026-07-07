package com.placement.commandcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String role;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;

    @Column(nullable = false)
    private String stage; // Applied, OA, Interview, Offer, Rejected

    @Column(name = "package_band")
    private String packageBand;

    @Column(name = "job_url")
    private String jobUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reminder_date")
    private LocalDate reminderDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (stage == null) {
            stage = "Applied";
        }
    }
}
