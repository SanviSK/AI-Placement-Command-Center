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
public class ReadinessScoreResponse {
    private Integer overallScore;
    private Breakdown breakdown;
    private LocalDateTime lastCalculatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Breakdown {
        private AcademicsDetail academics;
        private DsaDetail dsaProgress;
        private ProjectsDetail projects;
        private SkillMatchDetail skillMatch;
        private ResumeCompletenessDetail resumeCompleteness;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AcademicsDetail {
        private Integer score;
        private Double weight;
        private Double cgpa;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DsaDetail {
        private Integer score;
        private Double weight;
        private Integer problemsSolved;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectsDetail {
        private Integer score;
        private Double weight;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMatchDetail {
        private Integer score;
        private Double weight;
        private List<String> matchedSkills;
        private List<String> missingSkills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeCompletenessDetail {
        private Integer score;
        private Double weight;
        private List<String> missingFields;
    }
}
