package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class StudentProjectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private List<String> techStack;

    private Long sourceRecommendationId;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(planned|in-progress|completed)$", message = "Status must be 'planned', 'in-progress', or 'completed'")
    private String status;
}
