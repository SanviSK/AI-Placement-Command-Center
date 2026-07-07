package com.placement.commandcenter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DsaProgressResponse {
    private Integer totalSolved;
    private Map<String, Integer> byDifficulty;
    private Map<String, Integer> byTopic;
    private Integer currentStreak;
    private Integer longestStreak;
    private List<HistoryDto> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryDto {
        private LocalDate date;
        private Integer solvedCount;
    }
}
