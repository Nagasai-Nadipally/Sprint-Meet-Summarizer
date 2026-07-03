package com.meetingnotes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class OpenAiConfig {

    @Value("${app.openai.api-key}")
    private String apiKey;

    @Value("${app.openai.base-url}")
    private String baseUrl;

    /**
     * Dedicated RestClient for OpenAI. Long read timeout because Whisper
     * transcription of a long recording can take a while.
     */
    @Bean("openAiRestClient")
    public RestClient openAiRestClient() {
        var settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(15))
                .withReadTimeout(Duration.ofMinutes(5));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }
}
