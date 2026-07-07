package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswerResponse {
    private FeedbackDto feedback;
    private QuestionDto nextQuestion; // null if session ends
    private Integer questionNumber;
    private Integer totalQuestions;
}
