package com.contest_manager.contest_service.repository;

import com.contest_manager.contest_service.entity.Problem;
import com.contest_manager.contest_service.entity.ProblemVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, UUID> {
    List<Problem> findByVisibility(ProblemVisibility visibility);

    List<Problem> findByVisibilityOrVisibilityIsNull(ProblemVisibility visibility);

    List<Problem> findByCreatedBy(String createdBy);

    List<Problem> findByCreatedByOrVisibility(String createdBy, ProblemVisibility visibility);

    List<Problem> findByCreatedByOrVisibilityOrVisibilityIsNull(String createdBy, ProblemVisibility visibility);
}
