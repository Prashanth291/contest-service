package com.contest_manager.contest_service.repository;

import com.contest_manager.contest_service.entity.Contest;
import com.contest_manager.contest_service.entity.ContestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ContestRepository extends JpaRepository<Contest, UUID> {
    List<Contest> findByStatusAndStartTimeLessThanEqual(ContestStatus status, LocalDateTime time);
}
