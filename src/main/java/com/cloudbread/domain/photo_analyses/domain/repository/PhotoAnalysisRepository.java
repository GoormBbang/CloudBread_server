package com.cloudbread.domain.photo_analyses.domain.repository;

import com.cloudbread.domain.photo_analyses.domain.entity.PhotoAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoAnalysisRepository extends JpaRepository<PhotoAnalysis, Long> {
}
