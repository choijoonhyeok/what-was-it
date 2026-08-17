package com.whatwasit.backend.analysis.candidate.repository;

import com.whatwasit.backend.analysis.candidate.entity.AnalysisCandidateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisCandidateRepository
        extends JpaRepository<AnalysisCandidateEntity, Long>
   {
}