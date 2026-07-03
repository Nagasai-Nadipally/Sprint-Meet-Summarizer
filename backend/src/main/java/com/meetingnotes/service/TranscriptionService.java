package com.meetingnotes.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.meetingnotes.exception.AiProcessingException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Wraps the OpenAI audio transcription endpoint (Whisper).
 */
@Service
public class TranscriptionService {

    private final RestClient openAi;
    private final String model;

    public TranscriptionService(
            @Qualifier("openAiRestClient") RestClient openAi,
            @Value("${app.openai.whisper-model}") String model
    ) {
        this.openAi = openAi;
        this.model = model;
    }

    public Result transcribe(Resource audio) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", audio);
        form.add("model", model);
        // verbose_json gives us the detected language alongside the text.
        form.add("response_format", "verbose_json");

        try {
            WhisperResponse response = openAi.post()
                    .uri("/v1/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(WhisperResponse.class);

            if (response == null || response.text() == null || response.text().isBlank()) {
                throw new AiProcessingException("Whisper returned an empty transcript");
            }
            return new Result(response.text().trim(), response.language());
        } catch (AiProcessingException e) {
            throw e;
        } catch (Exception e) {
            throw new AiProcessingException("Transcription failed: " + e.getMessage(), e);
        }
    }

    public record Result(String text, String language) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WhisperResponse(String text, String language) {}
}
