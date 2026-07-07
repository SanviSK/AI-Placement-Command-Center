package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {
    private Long id;
    private String email;
    private String name;
    private String college;
    private String branch;
    private String batch;
    private String resumeUrl;
    private String skills;
    private String targetCompanies;
    private Double marks;
    private Integer readinessScore;
}
