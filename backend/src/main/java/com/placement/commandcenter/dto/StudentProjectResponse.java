package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProjectResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> techStack;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long sourceRecommendationId;
}
