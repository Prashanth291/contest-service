package com.contest_manager.contest_service.scheduler;

import com.contest_manager.contest_service.entity.Contest;
import com.contest_manager.contest_service.entity.ContestStatus;
import com.contest_manager.contest_service.repository.ContestRepository;
import com.contest_manager.contest_service.service.ContestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContestScheduler {

    private final ContestRepository contestRepository;
    private final ContestService contestService;

    // Runs every minute at the 0th second
    @Scheduled(cron = "0 * * * * *")
    public void checkAndStartContests() {
        log.debug("Scheduler running: Checking for scheduled contests to start...");

        // Fetch contests that are SCHEDULED and their start time has passed
        // Note: Using UTC (ZoneOffset.UTC) is highly recommended for server timestamps
        List<Contest> readyToStart = contestRepository.findByStatusAndStartTimeLessThanEqual(
                ContestStatus.SCHEDULED, LocalDateTime.now(ZoneOffset.UTC)
        );

        for (Contest contest : readyToStart) {
            try {
                log.info("Auto-starting contest: {}", contest.getId());
                contestService.startContest(contest.getId().toString());
            } catch (Exception e) {
                // Catch exceptions per contest so one failing doesn't stop the rest in the loop
                log.error("Failed to auto-start contest {}: {}", contest.getId(), e.getMessage());
            }
        }
    }
}