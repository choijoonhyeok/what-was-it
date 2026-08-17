package com.whatwasit.backend.analysis.candidate.entity;

import com.whatwasit.backend.analysis.entity.AnalysisEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "analysis_candidate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnalysisCandidateEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long candidateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisEntity analysis;

    @Column(nullable = false)
    private String candidateName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double score;

    @Column(columnDefinition = "TEXT")
    private String reason;

}
