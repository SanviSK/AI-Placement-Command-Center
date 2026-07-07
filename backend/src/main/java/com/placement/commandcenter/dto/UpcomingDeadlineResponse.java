package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingDeadlineResponse {
    private Long id;
    private String companyName;
    private String role;
    private String stage;
    private LocalDate reminderDate;
    private Long daysRemaining;
}
