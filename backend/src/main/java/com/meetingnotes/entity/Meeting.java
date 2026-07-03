package com.meetingnotes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    /** Original uploaded filename, e.g. "sprint-planning.mp3". */
    @Column(nullable = false)
    private String originalFilename;

    /** Storage key / path returned by the StorageService. */
    @Column(nullable = false)
    private String storageKey;

    private long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.UPLOADED;

    /** Populated when status == FAILED so the UI can show what happened. */
    @Column(length = 1000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Transcript transcript;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Summary summary;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ActionItem> actionItems = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
