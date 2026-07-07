package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedContentDto {

    private String summary;

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Builder.Default
    private List<ExperienceItem> experience = new ArrayList<>();

    @Builder.Default
    private List<ProjectItem> projects = new ArrayList<>();

    @Builder.Default
    private List<EducationItem> education = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExperienceItem {
        private String company;
        private String role;
        private String duration;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectItem {
        private String name;
        private String description;
        private String technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationItem {
        private String school;
        private String degree;
        private String year;
    }
}
