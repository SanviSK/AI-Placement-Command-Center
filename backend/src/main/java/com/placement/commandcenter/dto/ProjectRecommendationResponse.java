package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRecommendationResponse {
    private Long id;
    private String title;
    private String description;
    private List<String> techStack;
    private String reasoning;
    private String difficulty; // beginner, intermediate, advanced
    private Integer estimatedWeeks;
}
