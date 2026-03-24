package com.contest_manager.contest_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "problems")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String inputFormat;

    @Column(columnDefinition = "TEXT")
    private String outputFormat;

    @Column(columnDefinition = "TEXT")
    private String constraints;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private Integer baseScore;
    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCase> testCases = new ArrayList<>();
}