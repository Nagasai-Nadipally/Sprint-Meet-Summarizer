package com.meetingnotes.service;

import com.meetingnotes.dto.ai.AiMeetingAnalysis;
import com.meetingnotes.entity.MeetingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Runs the transcription + summarization pipeline off the request thread.
 * Each DB write is delegated to {@link MeetingStepService} so it commits in its
 * own transaction and the dashboard can show live progress.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingProcessingService {

    private final StorageService storageService;
    private final TranscriptionService transcriptionService;
    private final AiSummaryService aiSummaryService;
    private final MeetingStepService steps;

    @Async("aiTaskExecutor")
    public void process(Long meetingId) {
        log.info("Starting processing for meeting {}", meetingId);
        try {
            // 1. Transcribe with Whisper
            steps.updateStatus(meetingId, MeetingStatus.TRANSCRIBING);
            String storageKey = steps.getStorageKey(meetingId);
            Resource audio = storageService.load(storageKey);

            TranscriptionService.Result transcript = transcriptionService.transcribe(audio);
            steps.saveTranscript(meetingId, transcript.text(), transcript.language());

            // 2. Summarize + extract action items with GPT
            AiMeetingAnalysis analysis = aiSummaryService.analyze(transcript.text());
            steps.saveAnalysis(meetingId, analysis);

            log.info("Finished processing for meeting {}", meetingId);
        } catch (Exception e) {
            log.error("Processing failed for meeting {}: {}", meetingId, e.getMessage(), e);
            steps.markFailed(meetingId, e.getMessage());
        }
    }
}
