package com.contest_manager.contest_service.dto;

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
public class ContestStartedEvent {
    private String contestId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<ProblemInfo> problems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProblemInfo {
        private String problemId;
        private int order;
        private String label;
        private String title;
        private int score;
    }
}