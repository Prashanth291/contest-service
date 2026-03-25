package com.contest_manager.contest_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ContestRequest {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String createdBy;
    private String password;
}