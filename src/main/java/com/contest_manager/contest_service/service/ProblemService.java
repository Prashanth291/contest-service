package com.contest_manager.contest_service.service;

import com.contest_manager.contest_service.dto.ProblemRequest;
import com.contest_manager.contest_service.dto.ProblemResponse;
import com.contest_manager.contest_service.entity.Problem;
import com.contest_manager.contest_service.entity.ProblemVisibility;
import com.contest_manager.contest_service.entity.TestCase;
import com.contest_manager.contest_service.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    @Transactional(readOnly = true)
    public List<ProblemResponse> getProblems(String createdBy, ProblemVisibility visibility,
            boolean includeOwnPrivate) {
        List<Problem> problems;
        if (visibility != null) {
            problems = problemRepository.findByVisibilityOrVisibilityIsNull(visibility);
        } else if (includeOwnPrivate && createdBy != null && !createdBy.isBlank()) {
            problems = problemRepository.findByCreatedByOrVisibilityOrVisibilityIsNull(createdBy,
                    ProblemVisibility.GLOBAL);
        } else {
            problems = problemRepository.findByVisibilityOrVisibilityIsNull(ProblemVisibility.GLOBAL);
        }

        return problems.stream()
                .map(this::mapToProblemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProblemResponse createProblem(ProblemRequest request) {
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .inputFormat(request.getInputFormat())
                .outputFormat(request.getOutputFormat())
                .constraints(request.getConstraints())
                .difficulty(request.getDifficulty())
                .baseScore(request.getBaseScore())
                .createdBy(request.getCreatedBy())
                .visibility(request.getVisibility() != null ? request.getVisibility() : ProblemVisibility.PRIVATE)
                .build();

        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            List<TestCase> testCases = request.getTestCases().stream().map(tcDto -> TestCase.builder()
                    .problem(problem)
                    .input(tcDto.getInput())
                    .expectedOutput(tcDto.getExpectedOutput())
                    .isSample(tcDto.getIsSample())
                    .build()).collect(Collectors.toList());
            problem.setTestCases(testCases);
        }

        Problem savedProblem = problemRepository.save(problem);
        return mapToProblemResponse(savedProblem);
    }

    @Transactional(readOnly = true)
    public ProblemResponse getProblem(String id, String requesterId) {
        Problem problem = problemRepository.findById(UUID.fromString(id))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found with ID: " + id));

        boolean isOwner = requesterId != null && !requesterId.isBlank() && requesterId.equals(problem.getCreatedBy());
        ProblemVisibility effectiveVisibility = problem.getVisibility() == null ? ProblemVisibility.GLOBAL
                : problem.getVisibility();

        if (effectiveVisibility == ProblemVisibility.PRIVATE && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Problem is private");
        }

        return mapToProblemResponse(problem);
    }

    @Transactional
    public ProblemResponse updateProblem(String id, ProblemRequest request) {
        Problem problem = problemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("Problem not found with ID: " + id));

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        problem.setInputFormat(request.getInputFormat());
        problem.setOutputFormat(request.getOutputFormat());
        problem.setConstraints(request.getConstraints());
        problem.setDifficulty(request.getDifficulty());
        problem.setBaseScore(request.getBaseScore());
        if (request.getVisibility() != null) {
            problem.setVisibility(request.getVisibility());
        }

        // Replace old test cases with new ones
        problem.getTestCases().clear();
        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            List<TestCase> testCases = request.getTestCases().stream().map(tcDto -> TestCase.builder()
                    .problem(problem)
                    .input(tcDto.getInput())
                    .expectedOutput(tcDto.getExpectedOutput())
                    .isSample(tcDto.getIsSample())
                    .build()).collect(Collectors.toList());
            problem.getTestCases().addAll(testCases);
        }

        Problem updatedProblem = problemRepository.save(problem);
        return mapToProblemResponse(updatedProblem);
    }

    @Transactional
    public void deleteProblem(String id) {
        if (!problemRepository.existsById(UUID.fromString(id))) {
            throw new RuntimeException("Problem not found with ID: " + id);
        }
        problemRepository.deleteById(UUID.fromString(id));
    }

    private ProblemResponse mapToProblemResponse(Problem problem) {
        List<ProblemResponse.TestCaseResponse> testCaseResponses = problem.getTestCases().stream()
                .map(tc -> ProblemResponse.TestCaseResponse.builder()
                        .id(tc.getId().toString())
                        .input(tc.getInput())
                        .expectedOutput(tc.getExpectedOutput())
                        .isSample(tc.getIsSample())
                        .build())
                .collect(Collectors.toList());

        return ProblemResponse.builder()
                .id(problem.getId().toString())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .inputFormat(problem.getInputFormat())
                .outputFormat(problem.getOutputFormat())
                .constraints(problem.getConstraints())
                .difficulty(problem.getDifficulty())
                .baseScore(problem.getBaseScore())
                .createdBy(problem.getCreatedBy())
                .createdAt(problem.getCreatedAt())
                .visibility(problem.getVisibility() == null ? ProblemVisibility.GLOBAL : problem.getVisibility())
                .testCases(testCaseResponses)
                .build();
    }
}