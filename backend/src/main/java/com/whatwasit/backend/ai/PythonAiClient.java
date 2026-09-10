package com.whatwasit.backend.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PythonAiClient {

    private final RestClient restClient;

    public AnalyzeResponse analyze(String merchantName) {

        return restClient.post()
                .uri("/analyze")
                .body(new AnalyzeRequest(merchantName))
                .retrieve()
                .body(AnalyzeResponse.class);
    }

    public record AnalyzeRequest(
            @JsonProperty("merchant_name")
            String merchantName
    ) {
    }

    public record AnalyzeResponse(
            String originalName,
            String normalizedName,
            List<CandidateEvidence> candidates
    ) {
    }

    public record CandidateEvidence(
            Candidate candidate,
            List<SearchResult> searchResults,
            String ragContext
    ) {
    }

    public record Candidate(
            Long id,
            String name,
            String category,
            String description,
            double score
    ) {
    }

    public record SearchResult(
            String title,
            String link,
            String description
    ) {
    }
}


