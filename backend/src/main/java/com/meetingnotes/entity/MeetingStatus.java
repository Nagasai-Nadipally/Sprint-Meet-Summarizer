package com.meetingnotes.entity;

/**
 * Tracks where a meeting is in the transcription + AI pipeline.
 */
public enum MeetingStatus {
    UPLOADED,      // file saved, nothing processed yet
    TRANSCRIBING,  // sent to Whisper
    SUMMARIZING,   // transcript sent to GPT
    COMPLETED,     // summary + action items ready
    FAILED         // something went wrong during processing
}
