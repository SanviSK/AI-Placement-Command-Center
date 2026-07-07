package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedResumeResponse {
    private Long id;
    private Long targetCompanyId;
    private String jobTitle;
    private GeneratedContentDto generatedContent;
    private List<String> matchedKeywords;
    private boolean isActive;
    private LocalDateTime createdAt;
}
