package com.whatwasit.backend.analysis.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class AiResponseDTO {

    private String summary;
    private Double confidence;
    private List<CandidateDTO> candidates;

}

