package com.placement.commandcenter.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SemesterMarkRequest {

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 8, message = "Semester cannot exceed 8")
    private Integer semester;

    @NotNull(message = "SGPA is required")
    @Min(value = 0, message = "SGPA cannot be negative")
    @Max(value = 10, message = "SGPA cannot exceed 10.0")
    private Double sgpa;
}
