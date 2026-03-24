package com.contest_manager.contest_service.dto;

import com.contest_manager.contest_service.entity.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse {
    private String id;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Difficulty difficulty;
    private Integer baseScore;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<TestCaseResponse> testCases;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseResponse {
        private String id;
        private String input;
        private String expectedOutput;
        private Boolean isSample;
    }
}
