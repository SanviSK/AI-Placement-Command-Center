package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeListItemResponse {
    private Long id;
    private Long targetCompanyId;
    private String jobTitle;
    private LocalDateTime createdAt;
    private boolean isActive;
}
