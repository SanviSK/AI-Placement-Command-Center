package com.placement.commandcenter.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_stage_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Application application;

    @Column(nullable = false)
    private String stage;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
}
