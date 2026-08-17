package com.whatwasit.backend.analysis.candidate.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CandidateDTO  {

    private String name;
    private String description;
    private Double score;
    private String reason;

}
