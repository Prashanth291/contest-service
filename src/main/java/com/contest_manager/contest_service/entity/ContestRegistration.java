package com.contest_manager.contest_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contest_registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"contest_id", "user_id"})
})
public class ContestRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}