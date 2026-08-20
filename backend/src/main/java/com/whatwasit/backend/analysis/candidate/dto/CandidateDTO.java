package com.whatwasit.backend.analysis.candidate.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CandidateDTO  {

    private String name;
    private String description;
    private Double score;
    private String reason;

}
