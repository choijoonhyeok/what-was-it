package com.whatwasit.backend.analysis.repository;

import com.whatwasit.backend.analysis.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {
}
