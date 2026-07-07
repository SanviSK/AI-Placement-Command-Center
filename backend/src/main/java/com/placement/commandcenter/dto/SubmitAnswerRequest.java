package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitAnswerRequest {
    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Answer text is required")
    private String answerText;
}
