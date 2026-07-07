package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartInterviewRequest {
    private Long targetCompanyId;

    @NotBlank(message = "Interview type is required")
    private String interviewType; // technical, hr, behavioral
}
