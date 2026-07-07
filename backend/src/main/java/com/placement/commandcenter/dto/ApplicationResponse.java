package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private String companyName;
    private String role;
    private LocalDate appliedDate;
    private String stage;
    private String packageBand;
    private String jobUrl;
    private String notes;
    private LocalDate reminderDate;
    private LocalDateTime updatedAt;
}
