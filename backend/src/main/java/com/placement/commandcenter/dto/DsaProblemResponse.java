package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DsaProblemResponse {
    private Long id;
    private String title;
    private String topic;
    private String difficulty;
    private String leetcodeUrl;
    private List<String> tags;
}
