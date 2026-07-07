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
public class InterviewTranscriptResponse {
    private Long sessionId;
    private String interviewType;
    private Long targetCompanyId;
    private String companyName;
    private String role;
    private List<TranscriptQuestionDto> questions;
    private Double overallScore;
    private LocalDateTime completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TranscriptQuestionDto {
        private String questionText;
        private String answerText;
        private FeedbackDto feedback;
    }
}
