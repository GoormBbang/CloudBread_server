package com.cloudbread.domain.photo_analyses.application;

import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisRequest;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PhotoAnalysisService {
    PhotoAnalysisResponse.UploadResponse upload(Long userId, MultipartFile file) throws Exception;
    void handleAiLabel(Long photoAnalysisId, PhotoAnalysisRequest.AiLabelRequest request) throws Exception;

    PhotoAnalysisResponse.ConfirmResponse confirm(Long userId, Long photoAnalysisId, Long selectedFoodId);
}
