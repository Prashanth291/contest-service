package com.contest_manager.contest_service.controller;

import com.contest_manager.contest_service.dto.ProblemRequest;
import com.contest_manager.contest_service.dto.ProblemResponse;
import com.contest_manager.contest_service.entity.ProblemVisibility;
import com.contest_manager.contest_service.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<List<ProblemResponse>> getProblems(
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) ProblemVisibility visibility,
            @RequestParam(defaultValue = "true") boolean includeOwnPrivate) {
        return ResponseEntity.ok(problemService.getProblems(createdBy, visibility, includeOwnPrivate));
    }

    @PostMapping
    public ResponseEntity<ProblemResponse> createProblem(@RequestBody ProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(problemService.createProblem(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemResponse> getProblem(@PathVariable String id,
            @RequestParam(required = false) String requesterId) {
        return ResponseEntity.ok(problemService.getProblem(id, requesterId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProblemResponse> updateProblem(@PathVariable String id, @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(problemService.updateProblem(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProblem(@PathVariable String id) {
        problemService.deleteProblem(id);
        return ResponseEntity.noContent().build();
    }
}