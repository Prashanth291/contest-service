package com.contest_manager.contest_service.repository;

import com.contest_manager.contest_service.entity.ContestProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContestProblemRepository extends JpaRepository<ContestProblem, UUID> {
    Optional<ContestProblem> findByContestIdAndProblemId(UUID contestId, UUID problemId);
}