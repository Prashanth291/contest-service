package com.contest_manager.contest_service.repository;

import com.contest_manager.contest_service.entity.ContestRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID; // Make sure to import this!

public interface ContestRegistrationRepository extends JpaRepository<ContestRegistration, String> {

    boolean existsByContestIdAndUserId(UUID contestId, String userId);
    Optional<ContestRegistration> findByContestIdAndUserId(UUID contestId, String userId);
}