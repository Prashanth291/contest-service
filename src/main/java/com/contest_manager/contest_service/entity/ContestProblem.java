package com.contest_manager.contest_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "contest_problems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContestProblem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private String label; // A, B, C...
    private Integer problemOrder;
    private Integer score;
}
