package com.contest_manager.contest_service.repository;

import com.contest_manager.contest_service.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, UUID> {
}