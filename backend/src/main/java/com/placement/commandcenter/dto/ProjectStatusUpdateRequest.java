package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(planned|in-progress|completed)$", message = "Status must be 'planned', 'in-progress', or 'completed'")
    private String status;

    private LocalDateTime completedAt;
}
