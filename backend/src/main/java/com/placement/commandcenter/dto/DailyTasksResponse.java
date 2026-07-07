package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTasksResponse {
    private LocalDate date;
    private List<TaskDto> tasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskDto {
        private Long problemId;
        private String title;
        private String topic;
        private String difficulty;
        private String leetcodeUrl;
        private String status; // pending, solved
    }
}
