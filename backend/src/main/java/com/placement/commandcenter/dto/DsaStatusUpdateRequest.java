package com.placement.commandcenter.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DsaStatusUpdateRequest {
    @Pattern(regexp = "^(solved|pending)$", message = "Status must be either 'solved' or 'pending'")
    private String status;
}
