package com.placement.commandcenter.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String college;
    private String branch;
    private String batch;
    private String skills;
    private String targetCompanies;
    private Double marks;
}
