package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ApplicationStageUpdateRequest {

    @NotBlank(message = "Stage is required")
    @Pattern(
        regexp = "^(Applied|OA|Interview|Offer|Rejected)$",
        message = "Stage must be one of: 'Applied', 'OA', 'Interview', 'Offer', or 'Rejected'"
    )
    private String stage;
}
