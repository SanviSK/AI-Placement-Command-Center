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
public class InterviewListItemResponse {
    private Long sessionId;
    private String interviewType;
    private Long targetCompanyId;
    private String companyName;
    private String role;
    private Double overallScore;
    private LocalDateTime completedAt;
    private Integer questionCount;
}
