package com.contest_manager.contest_service.dto;

import com.contest_manager.contest_service.entity.Difficulty;
import lombok.Data;
import java.util.List;

@Data
public class ProblemRequest {
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private Difficulty difficulty;
    private Integer baseScore;
    private String createdBy;
    private List<TestCaseDto> testCases;

    @Data
    public static class TestCaseDto {
        private String input;
        private String expectedOutput;
        private Boolean isSample;
    }
}