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
public class ParsedResumeData {

    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Builder.Default
    private List<Education> education = new ArrayList<>();

    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    @Builder.Default
    private List<Experience> experience = new ArrayList<>();

    @Builder.Default
    private List<String> certifications = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Education {
        private String school;
        private String degree;
        private String year;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Project {
        private String name;
        private String description;
        private String technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Experience {
        private String company;
        private String role;
        private String duration;
        private String description;
    }
}
