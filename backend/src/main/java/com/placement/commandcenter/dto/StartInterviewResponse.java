package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartInterviewResponse {
    private Long sessionId;
    private QuestionDto question;
    private Integer questionNumber;
    private Integer totalQuestions;
}
