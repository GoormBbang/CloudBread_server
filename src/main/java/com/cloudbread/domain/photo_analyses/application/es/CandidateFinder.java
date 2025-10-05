package com.cloudbread.domain.photo_analyses.application.es;

import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;

import java.util.List;

public interface CandidateFinder {
    List<PhotoAnalysisResponse.CandidateItem> find(String query, int limit);
}
