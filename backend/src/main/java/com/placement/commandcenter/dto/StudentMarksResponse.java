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
public class StudentMarksResponse {
    private List<SemesterMarkDto> marks;
    private Double overallCgpa;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SemesterMarkDto {
        private Long id;
        private Integer semester;
        private Double sgpa;
    }
}
