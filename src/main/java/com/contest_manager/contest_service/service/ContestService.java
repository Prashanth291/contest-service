package com.contest_manager.contest_service.service;

import com.contest_manager.contest_service.dto.*;
import com.contest_manager.contest_service.entity.Contest;
import com.contest_manager.contest_service.entity.ContestProblem;
import com.contest_manager.contest_service.entity.ContestStatus;
import com.contest_manager.contest_service.entity.Problem;
import com.contest_manager.contest_service.repository.ContestProblemRepository;
import com.contest_manager.contest_service.repository.ContestRepository;
import com.contest_manager.contest_service.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContestService {

    private final ContestRepository contestRepository;
    private final ProblemRepository problemRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestEventPublisher eventPublisher;

    @Transactional
    public ContestResponse createContest(ContestRequest request) {
        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ContestStatus.DRAFT) // Always starts as DRAFT
                .createdBy(request.getCreatedBy())
                .build();

        Contest savedContest = contestRepository.save(contest);
        return mapToContestResponse(savedContest);
    }

    @Transactional(readOnly = true)
    public List<ContestResponse> getAllContests() {
        return contestRepository.findAll().stream()
                .map(this::mapToContestResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContestResponse getContest(String id) {
        Contest contest = contestRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Contest not found with ID: " + id));
        return mapToContestResponse(contest);
    }

    @Transactional
    public ContestResponse updateContest(String id, ContestRequest request) {
        Contest contest = contestRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Contest not found with ID: " + id));

        // Don't allow updating times if it's already active or ended
        if (contest.getStatus() == ContestStatus.ACTIVE || contest.getStatus() == ContestStatus.ENDED) {
            throw new RuntimeException("Cannot update a contest that is ACTIVE or ENDED");
        }

        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());

        // Auto-promote to SCHEDULED if times are set
        if (contest.getStatus() == ContestStatus.DRAFT && contest.getStartTime() != null) {
            contest.setStatus(ContestStatus.SCHEDULED);
        }

        Contest updatedContest = contestRepository.save(contest);
        return mapToContestResponse(updatedContest);
    }

    @Transactional
    public void deleteContest(String id) {
        if (!contestRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Contest not found");
        }
        contestRepository.deleteById(UUID.fromString(id));
    }

    // --- Problem Assignment Logic ---

    @Transactional
    public void assignProblemToContest(String contestId, AssignProblemRequest request) {
        Contest contest = contestRepository.findById(UUID.fromString(contestId))
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        Problem problem = problemRepository.findById(UUID.fromString(request.getProblemId()))
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // Check if already assigned
        if (contestProblemRepository.findByContestIdAndProblemId(contest.getId(), problem.getId()).isPresent()) {
            throw new RuntimeException("Problem is already assigned to this contest");
        }

        ContestProblem contestProblem = ContestProblem.builder()
                .contest(contest)
                .problem(problem)
                .label(request.getLabel())
                .problemOrder(request.getProblemOrder())
                .score(request.getScore() != null ? request.getScore() : problem.getBaseScore())
                .build();

        contestProblemRepository.save(contestProblem);
    }

    @Transactional
    public void removeProblemFromContest(String contestId, String problemId) {
        ContestProblem cp = contestProblemRepository
                .findByContestIdAndProblemId(UUID.fromString(contestId), UUID.fromString(problemId))
                .orElseThrow(() -> new RuntimeException("Problem not found in this contest"));

        contestProblemRepository.delete(cp);
    }

    // --- Core Kafka Publishing Logic ---

    @Transactional
    public void startContest(String id) {
        Contest contest = contestRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        if (contest.getStatus() == ContestStatus.ACTIVE) {
            throw new RuntimeException("Contest is already active");
        }

        // 1. Map to the exact Kafka Event structure
        List<ContestStartedEvent.ProblemInfo> problemInfos = contest.getContestProblems().stream()
                .map(cp -> ContestStartedEvent.ProblemInfo.builder()
                        .problemId(cp.getProblem().getId().toString())
                        .order(cp.getProblemOrder())
                        .label(cp.getLabel())
                        .title(cp.getProblem().getTitle())
                        .score(cp.getScore() != null ? cp.getScore() : cp.getProblem().getBaseScore())
                        .build())
                .collect(Collectors.toList());

        ContestStartedEvent event = ContestStartedEvent.builder()
                .contestId(contest.getId().toString())
                .title(contest.getTitle())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .problems(problemInfos)
                .build();

        // 2. Publish to Kafka
        eventPublisher.publishContestStartedEvent(event);

        // 3. Update Status
        contest.setStatus(ContestStatus.ACTIVE);
        contestRepository.save(contest);

        log.info("Contest {} successfully started and event published.", id);
    }

    // --- Helper Method ---
    private ContestResponse mapToContestResponse(Contest contest) {
        List<ContestResponse.ContestProblemDto> problemDtos = contest.getContestProblems().stream()
                .map(cp -> ContestResponse.ContestProblemDto.builder()
                        .problemId(cp.getProblem().getId().toString())
                        .title(cp.getProblem().getTitle())
                        .label(cp.getLabel())
                        .problemOrder(cp.getProblemOrder())
                        .score(cp.getScore() != null ? cp.getScore() : cp.getProblem().getBaseScore())
                        .build())
                .collect(Collectors.toList());

        return ContestResponse.builder()
                .id(contest.getId().toString())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .status(contest.getStatus())
                .createdBy(contest.getCreatedBy())
                .problems(problemDtos)
                .build();
    }
}