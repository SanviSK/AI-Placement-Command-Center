package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TargetCompanyRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role is required")
    private String role;

    private String packageBand;

    private LocalDate applicationDeadline;
}
