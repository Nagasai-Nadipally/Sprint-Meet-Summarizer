package com.meetingnotes.dto.meeting;

import com.meetingnotes.entity.Meeting;
import com.meetingnotes.entity.MeetingStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Full detail view for a single meeting. Any of the nested pieces may be null
 * while the meeting is still being processed.
 */
public record MeetingDetailResponse(
        Long id,
        String title,
        String originalFilename,
        MeetingStatus status,
        String errorMessage,
        String transcript,
        String overview,
        List<String> keyPoints,
        List<String> followUpQuestions,
        List<ActionItemResponse> actionItems,
        Instant createdAt,
        Instant updatedAt
) {
    public static MeetingDetailResponse from(Meeting m) {
        var summary = m.getSummary();
        return new MeetingDetailResponse(
                m.getId(),
                m.getTitle(),
                m.getOriginalFilename(),
                m.getStatus(),
                m.getErrorMessage(),
                m.getTranscript() != null ? m.getTranscript().getContent() : null,
                summary != null ? summary.getOverview() : null,
                summary != null ? new ArrayList<>(summary.getKeyPoints()) : Collections.emptyList(),
                summary != null ? new ArrayList<>(summary.getFollowUpQuestions()) : Collections.emptyList(),
                m.getActionItems().stream().map(ActionItemResponse::from).toList(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
