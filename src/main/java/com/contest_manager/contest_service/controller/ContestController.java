package com.contest_manager.contest_service.controller;

import com.contest_manager.contest_service.dto.AssignProblemRequest;
import com.contest_manager.contest_service.dto.ContestRequest;
import com.contest_manager.contest_service.dto.ContestResponse;
import com.contest_manager.contest_service.dto.JoinContestRequest;
import com.contest_manager.contest_service.dto.ProblemResponse;
import com.contest_manager.contest_service.service.ContestService;
import com.contest_manager.contest_service.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;
    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<ContestResponse> createContest(@RequestBody ContestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contestService.createContest(request));
    }

    @GetMapping
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        return ResponseEntity.ok(contestService.getAllContests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestResponse> getContest(@PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String requesterId) {
        return ResponseEntity.ok(contestService.getContest(id, requesterId));
    }

    @GetMapping("/{id}/problems/{problemId}")
    public ResponseEntity<ProblemResponse> getContestProblem(
            @PathVariable String id,
            @PathVariable String problemId,
            @RequestHeader(value = "X-User-Id", required = false) String requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole) {
        boolean isAdmin = requesterRole != null && requesterRole.equalsIgnoreCase("ADMIN");
        if (!contestService.canAccessContestProblem(UUID.fromString(id), requesterId, isAdmin, UUID.fromString(problemId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(problemService.getProblemWithoutVisibilityCheck(problemId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContestResponse> updateContest(@PathVariable String id, @RequestBody ContestRequest request) {
        return ResponseEntity.ok(contestService.updateContest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContest(@PathVariable String id) {
        contestService.deleteContest(id);
        return ResponseEntity.noContent().build();
    }

    // --- Problem Assignment Endpoints ---

    @PostMapping("/{id}/problems")
    public ResponseEntity<Void> assignProblemToContest(
            @PathVariable String id,
            @RequestBody AssignProblemRequest request) {
        contestService.assignProblemToContest(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/problems/{problemId}")
    public ResponseEntity<Void> removeProblemFromContest(
            @PathVariable String id,
            @PathVariable String problemId) {
        contestService.removeProblemFromContest(id, problemId);
        return ResponseEntity.noContent().build();
    }

    // --- Manual Start Endpoint ---

    @PostMapping("/{id}/start")
    public ResponseEntity<String> startContestManually(@PathVariable String id) {
        contestService.startContest(id);
        return ResponseEntity.ok("Contest started and event published to Kafka successfully.");
    }

    @PostMapping("/join/{joinCode}")
    public ResponseEntity<String> joinContest(
            @PathVariable String joinCode,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody JoinContestRequest request) {
        contestService.registerForContest(joinCode, userId, request);
        return ResponseEntity.ok("Successfully joined the contest!");
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    public ResponseEntity<Void> kickParticipant(
            @PathVariable UUID id,
            @PathVariable String participantId,
            @RequestHeader("X-User-Id") String requesterId) {
        contestService.kickUser(id, participantId, requesterId);
        return ResponseEntity.noContent().build();
    }
}