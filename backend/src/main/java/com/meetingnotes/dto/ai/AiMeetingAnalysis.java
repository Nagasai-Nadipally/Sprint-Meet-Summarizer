package com.meetingnotes.dto.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * The exact JSON shape we instruct GPT to return. Jackson deserializes the
 * model's response directly into this record. {@code @JsonIgnoreProperties}
 * keeps us resilient if the model adds extra keys.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiMeetingAnalysis(
        String summary,
        List<String> keyPoints,
        List<ActionItemDto> actionItems,
        List<String> followUpQuestions
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ActionItemDto(
            String owner,
            String task,
            // ISO date string (yyyy-MM-dd) or null if the model couldn't infer one
            String deadline
    ) {}
}
