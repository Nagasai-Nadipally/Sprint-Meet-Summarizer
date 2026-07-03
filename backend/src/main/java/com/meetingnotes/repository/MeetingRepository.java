package com.meetingnotes.repository;

import com.meetingnotes.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Meeting> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Case-insensitive keyword search across the meeting title, the summary
     * overview, and the raw transcript text. Scoped to a single owner.
     *
     * <p>Implemented as a native PostgreSQL query using ILIKE. This avoids
     * Hibernate's HQL translation of LOWER()/LIKE over large text columns,
     * which can fail during startup query validation.
     */
    @Query(value = """
            SELECT m.* FROM meetings m
            LEFT JOIN summaries s   ON s.meeting_id = m.id
            LEFT JOIN transcripts t ON t.meeting_id = m.id
            WHERE m.user_id = :ownerId
              AND (
                    m.title    ILIKE '%' || :keyword || '%'
                 OR s.overview ILIKE '%' || :keyword || '%'
                 OR t.content  ILIKE '%' || :keyword || '%'
              )
            ORDER BY m.created_at DESC
            """, nativeQuery = true)
    List<Meeting> searchByKeyword(@Param("ownerId") Long ownerId, @Param("keyword") String keyword);
}
