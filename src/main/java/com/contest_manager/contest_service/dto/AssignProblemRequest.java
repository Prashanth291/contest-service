package com.contest_manager.contest_service.dto;

import lombok.Data;

@Data
public class AssignProblemRequest {
    private String problemId;
    private String label; // e.g., "A", "B"
    private Integer problemOrder;
    private Integer score; // optional, overrides base_score if provided
}