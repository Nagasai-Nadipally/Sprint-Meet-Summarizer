package com.meetingnotes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "action_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    /** Person responsible, as extracted by the model. May be "Unassigned". */
    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, length = 2000)
    private String task;

    /** Nullable - the model won't always find a date. */
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ActionItemStatus status = ActionItemStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
