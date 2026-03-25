package com.contest_manager.contest_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "contests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(name = "join_code", unique = true)
    private String joinCode;

    @Column(name = "password")
    private String password;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ContestStatus status = ContestStatus.DRAFT;

    private String createdBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("problemOrder ASC")
    private List<ContestProblem> contestProblems = new ArrayList<>();

}