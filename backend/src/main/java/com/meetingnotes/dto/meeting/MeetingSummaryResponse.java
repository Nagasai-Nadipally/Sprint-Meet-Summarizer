package com.meetingnotes.dto.meeting;

import com.meetingnotes.entity.Meeting;
import com.meetingnotes.entity.MeetingStatus;

import java.time.Instant;

/**
 * Lightweight projection used in the dashboard list. Avoids shipping the full
 * transcript/summary payload for every row.
 */
public record MeetingSummaryResponse(
        Long id,
        String title,
        MeetingStatus status,
        int actionItemCount,
        Instant createdAt
) {
    public static MeetingSummaryResponse from(Meeting m) {
        return new MeetingSummaryResponse(
                m.getId(),
                m.getTitle(),
                m.getStatus(),
                m.getActionItems() == null ? 0 : m.getActionItems().size(),
                m.getCreatedAt()
        );
    }
}
