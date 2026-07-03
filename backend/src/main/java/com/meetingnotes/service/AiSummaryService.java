package com.meetingnotes.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meetingnotes.dto.ai.AiMeetingAnalysis;
import com.meetingnotes.exception.AiProcessingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sends a transcript to GPT and turns the response into a structured
 * {@link AiMeetingAnalysis}. We force JSON output with response_format and
 * deserialize it with Jackson rather than parsing free text.
 */
@Service
public class AiSummaryService {

    private static final String SYSTEM_PROMPT = """
            You are an assistant that analyzes meeting transcripts and returns a structured summary.
            Today's date is %s. Use it to resolve relative deadlines such as "by Friday" or
            "end of next week" into absolute calendar dates.

            Respond with a single JSON object and nothing else. Use exactly this schema:
            {
              "summary": "A concise paragraph (3-5 sentences) summarizing the meeting.",
              "keyPoints": ["Short bullet of a key discussion point", ...],
              "actionItems": [
                {
                  "owner": "Name of the person responsible, or \\"Unassigned\\" if none was named",
                  "task": "What needs to be done, phrased as a clear action",
                  "deadline": "yyyy-MM-dd or null if no deadline was mentioned"
                }
              ],
              "followUpQuestions": ["An open question the team should follow up on", ...]
            }

            Rules:
            - Only include action items that were actually discussed. Do not invent tasks.
            - If no action items exist, return an empty array.
            - "deadline" must be a valid ISO date (yyyy-MM-dd) or null. Never use prose.
            - Keep owners to a person's name only, no titles or extra words.
            """;

    private final RestClient openAi;
    private final ObjectMapper objectMapper;
    private final String model;

    public AiSummaryService(
            @Qualifier("openAiRestClient") RestClient openAi,
            ObjectMapper objectMapper,
            @Value("${app.openai.gpt-model}") String model
    ) {
        this.openAi = openAi;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public AiMeetingAnalysis analyze(String transcript) {
        String systemPrompt = SYSTEM_PROMPT.formatted(LocalDate.now());

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.2,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", "Transcript:\n\n" + transcript)
                )
        );

        ChatResponse response;
        try {
            response = openAi.post()
                    .uri("/v1/chat/completions")
                    .body(requestBody)
                    .retrieve()
                    .body(ChatResponse.class);
        } catch (Exception e) {
            throw new AiProcessingException("Summary generation failed: " + e.getMessage(), e);
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiProcessingException("GPT returned no choices");
        }

        String json = response.choices().get(0).message().content();
        try {
            return objectMapper.readValue(json, AiMeetingAnalysis.class);
        } catch (Exception e) {
            throw new AiProcessingException("Could not parse the model's JSON response", e);
        }
    }

    // --- minimal OpenAI chat-completions wire types ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(String role, String content) {}
}
