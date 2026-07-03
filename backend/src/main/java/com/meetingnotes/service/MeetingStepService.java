package com.meetingnotes.service;

import com.meetingnotes.dto.ai.AiMeetingAnalysis;
import com.meetingnotes.entity.*;
import com.meetingnotes.exception.ResourceNotFoundException;
import com.meetingnotes.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Each method here is its own transaction. The pipeline calls them one at a
 * time so that status changes (TRANSCRIBING -> SUMMARIZING -> COMPLETED) commit
 * independently and become visible to the dashboard while work is in progress.
 */
@Service
@RequiredArgsConstructor
public class MeetingStepService {

    private final MeetingRepository meetingRepository;

    @Transactional
    public void updateStatus(Long meetingId, MeetingStatus status) {
        Meeting meeting = require(meetingId);
        meeting.setStatus(status);
    }

    @Transactional(readOnly = true)
    public String getStorageKey(Long meetingId) {
        return require(meetingId).getStorageKey();
    }

    @Transactional
    public void saveTranscript(Long meetingId, String text, String language) {
        Meeting meeting = require(meetingId);
        Transcript transcript = Transcript.builder()
                .meeting(meeting)
                .content(text)
                .language(language)
                .build();
        meeting.setTranscript(transcript);
        meeting.setStatus(MeetingStatus.SUMMARIZING);
    }

    @Transactional
    public void saveAnalysis(Long meetingId, AiMeetingAnalysis analysis) {
        Meeting meeting = require(meetingId);

        Summary summary = Summary.builder()
                .meeting(meeting)
                .overview(analysis.summary())
                .keyPoints(analysis.keyPoints() != null ? analysis.keyPoints() : new ArrayList<>())
                .followUpQuestions(analysis.followUpQuestions() != null
                        ? analysis.followUpQuestions() : new ArrayList<>())
                .build();
        meeting.setSummary(summary);

        List<ActionItem> items = new ArrayList<>();
        if (analysis.actionItems() != null) {
            for (var dto : analysis.actionItems()) {
                items.add(ActionItem.builder()
                        .meeting(meeting)
                        .owner(dto.owner() == null || dto.owner().isBlank() ? "Unassigned" : dto.owner())
                        .task(dto.task())
                        .deadline(parseDate(dto.deadline()))
                        .status(ActionItemStatus.PENDING)
                        .build());
            }
        }
        meeting.getActionItems().clear();
        meeting.getActionItems().addAll(items);
        meeting.setStatus(MeetingStatus.COMPLETED);
    }

    @Transactional
    public void markFailed(Long meetingId, String message) {
        Meeting meeting = require(meetingId);
        meeting.setStatus(MeetingStatus.FAILED);
        meeting.setErrorMessage(message != null && message.length() > 990
                ? message.substring(0, 990) : message);
    }

    private Meeting require(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + meetingId));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank() || value.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null; // model returned something that wasn't a clean ISO date
        }
    }
}
