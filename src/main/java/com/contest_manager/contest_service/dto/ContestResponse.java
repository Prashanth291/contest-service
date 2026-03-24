package com.contest_manager.contest_service.dto;

import com.contest_manager.contest_service.entity.ContestStatus;
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
public class ContestResponse {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ContestStatus status;
    private String createdBy;
    private List<ContestProblemDto> problems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContestProblemDto {
        private String problemId;
        private String title;
        private String label; // A, B, C
        private Integer problemOrder;
        private Integer score; // The overridden score for this specific contest
    }
}