package com.meetingnotes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meeting_id", nullable = false, unique = true)
    private Meeting meeting;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String overview;

    /** Bullet list of key discussion points, stored in a child table. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "summary_key_points", joinColumns = @JoinColumn(name = "summary_id"))
    @Column(name = "point", length = 1000)
    @Builder.Default
    private List<String> keyPoints = new ArrayList<>();

    /** Open questions the team should follow up on. */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "summary_follow_up_questions", joinColumns = @JoinColumn(name = "summary_id"))
    @Column(name = "question", length = 1000)
    @Builder.Default
    private List<String> followUpQuestions = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
