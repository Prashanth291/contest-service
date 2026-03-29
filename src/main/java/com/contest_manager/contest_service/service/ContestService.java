package com.contest_manager.contest_service.service;

import com.contest_manager.contest_service.dto.*;
import com.contest_manager.contest_service.entity.*;
import com.contest_manager.contest_service.repository.ContestProblemRepository;
import com.contest_manager.contest_service.repository.ContestRegistrationRepository;
import com.contest_manager.contest_service.repository.ContestRepository;
import com.contest_manager.contest_service.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final ContestRegistrationRepository registrationRepository;

    @Transactional
    public ContestResponse createContest(ContestRequest request) {

        // Generate a random 8-character string formatted as XXXX-XXXX
        String rawCode = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String formattedCode = rawCode.substring(0, 4) + "-" + rawCode.substring(4, 8);

        Contest contest = Contest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ContestStatus.DRAFT) // Always starts as DRAFT
                .createdBy(request.getCreatedBy())
                .joinCode(formattedCode)
                .password(request.getPassword()) // Set the password
                .build();

        Contest savedContest = contestRepository.save(contest);
        return mapToContestResponse(savedContest, null);
    }

    @Transactional(readOnly = true)
    public List<ContestResponse> getAllContests() {
        return contestRepository.findAll().stream()
                .map(c -> mapToContestResponse(c, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContestResponse getContest(String id, String requesterId) {
        Contest contest = contestRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Contest not found with ID: " + id));
        return mapToContestResponse(contest, requesterId);
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
        return mapToContestResponse(updatedContest, null);
    }

    @Transactional
    public void deleteContest(String id) {
        if (!contestRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Contest not found");
        }
        contestRepository.deleteById(UUID.fromString(id));
    }

    /*
     * Contest Registration
     */
    public void registerForContest(String joinCode, String userId, JoinContestRequest request) {
        Contest contest = contestRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> new RuntimeException("Contest not found with code: " + joinCode));

        // Creator cannot register as participant
        if (contest.getCreatedBy() != null && contest.getCreatedBy().equals(userId)) {
            throw new RuntimeException("Contest creator cannot join as participant");
        }

        // 1. Check password
        if (contest.getPassword() != null && !contest.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Incorrect contest password");
        }

        // 2. Check if already registered
        if (registrationRepository.existsByContestIdAndUserId(contest.getId(), userId)) {
            throw new RuntimeException("User is already registered for this contest");
        }

        // 3. Save the registration
        ContestRegistration registration = ContestRegistration.builder()
                .contest(contest)
                .userId(userId)
                .registeredAt(LocalDateTime.now())
                .build();

        registrationRepository.save(registration);
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

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000) // Wakes up every 60 seconds
    @Transactional
    public void autoStartPendingContests() {
        LocalDateTime now = LocalDateTime.now();

        // Find all contests that haven't started yet but their time has arrived
        List<Contest> pendingContests = contestRepository.findAll().stream()
                .filter(c -> c.getStatus() == ContestStatus.DRAFT || c.getStatus() == ContestStatus.SCHEDULED)
                .filter(c -> c.getStartTime() != null && !now.isBefore(c.getStartTime()))
                .collect(Collectors.toList());

        for (Contest contest : pendingContests) {
            try {
                log.info("Alarm clock triggered! Auto-starting contest: {}", contest.getId());
                startContest(contest.getId().toString());
            } catch (Exception e) {
                log.error("Failed to auto-start contest: {}", contest.getId(), e);
            }
        }
    }

    // --- Helper Method ---
    private ContestResponse mapToContestResponse(Contest contest, String requesterId) {
        // Safe null check for the list
        List<ContestResponse.ContestProblemDto> problemDtos = contest.getContestProblems() == null
                ? new java.util.ArrayList<>()
                : contest.getContestProblems().stream()
                        .map(cp -> ContestResponse.ContestProblemDto.builder()
                                .problemId(cp.getProblem().getId().toString())
                                .title(cp.getProblem().getTitle())
                                .label(cp.getLabel())
                                .problemOrder(cp.getProblemOrder())
                                .score(cp.getScore() != null ? cp.getScore() : cp.getProblem().getBaseScore())
                                .build())
                        .collect(Collectors.toList());

        boolean registered = false;
        if (requesterId != null && !requesterId.isBlank()) {
            registered = registrationRepository.existsByContestIdAndUserId(contest.getId(), requesterId);
        }
        return ContestResponse.builder()
                .id(contest.getId().toString())
                .title(contest.getTitle())
                .description(contest.getDescription())
                .startTime(contest.getStartTime())
                .endTime(contest.getEndTime())
                .status(contest.getStatus())
                .createdBy(contest.getCreatedBy())
                .joinCode(contest.getJoinCode())
                .registered(registered)
                .requiresPassword(contest.getPassword() != null)
                .problems(problemDtos)
                .build();
    }

    public void kickUser(UUID contestId, String participantId, String requesterId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        // Only the creator can kick people
        if (!contest.getCreatedBy().equals(requesterId)) {
            throw new RuntimeException("Only the contest owner can remove participants");
        }

        ContestRegistration registration = registrationRepository.findByContestIdAndUserId(contestId, participantId)
                .orElseThrow(() -> new RuntimeException("Participant is not registered in this contest"));

        registrationRepository.delete(registration);
    }

    @Transactional(readOnly = true)
    public boolean canAccessContestProblem(UUID contestId, String requesterId, boolean isAdmin, UUID problemId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found"));

        // Admins always allowed
        if (isAdmin) {
            return true;
        }

        // Must be creator or registered participant
        boolean isCreator = requesterId != null && requesterId.equals(contest.getCreatedBy());
        boolean isParticipant = requesterId != null && registrationRepository.existsByContestIdAndUserId(contestId, requesterId);
        if (!isCreator && !isParticipant) {
            return false;
        }

        // Ensure problem assigned to contest
        return contestProblemRepository.findByContestIdAndProblemId(contestId, problemId).isPresent();
    }
}