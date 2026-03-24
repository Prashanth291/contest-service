package com.contest_manager.contest_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "test_cases")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestCase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    private Boolean isSample;
}
