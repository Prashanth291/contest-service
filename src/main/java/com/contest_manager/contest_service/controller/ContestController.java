package com.contest_manager.contest_service.controller;

import com.contest_manager.contest_service.dto.AssignProblemRequest;
import com.contest_manager.contest_service.dto.ContestRequest;
import com.contest_manager.contest_service.dto.ContestResponse;
import com.contest_manager.contest_service.service.ContestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestService contestService;

    @PostMapping
    public ResponseEntity<ContestResponse> createContest(@RequestBody ContestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contestService.createContest(request));
    }

    @GetMapping
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        return ResponseEntity.ok(contestService.getAllContests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestResponse> getContest(@PathVariable String id) {
        return ResponseEntity.ok(contestService.getContest(id));
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
}