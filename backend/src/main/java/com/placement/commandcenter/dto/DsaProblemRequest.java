package com.placement.commandcenter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class DsaProblemRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Difficulty is required")
    private String difficulty; // EASY, MEDIUM, HARD

    @NotBlank(message = "LeetCode URL is required")
    private String leetcodeUrl;

    private List<String> tags;
}
