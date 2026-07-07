package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplicationRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Role is required")
    private String role;

    @NotNull(message = "Applied date is required")
    private LocalDate appliedDate;

    private String packageBand;
    private String jobUrl;
    private String notes;
    private LocalDate reminderDate;
}
