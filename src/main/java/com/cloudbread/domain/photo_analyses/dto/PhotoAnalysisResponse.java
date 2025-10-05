package com.cloudbread.domain.photo_analyses.dto;

import com.cloudbread.domain.photo_analyses.application.PhotoAnalysisService;
import com.cloudbread.domain.photo_analyses.domain.enums.PhotoAnalysisStatus;

public class PhotoAnalysisResponse {
    public record UploadResponse(Long photoAnalysisId, String imageUrl, PhotoAnalysisStatus status) {}
}
