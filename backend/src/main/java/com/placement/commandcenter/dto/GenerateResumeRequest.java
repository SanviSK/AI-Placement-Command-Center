package com.placement.commandcenter.dto;

import lombok.Data;

@Data
public class GenerateResumeRequest {
    private Long targetCompanyId;
    private String jobDescription;
}
