package com.cloudbread.domain.photo_analyses.api;


import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.photo_analyses.application.PhotoAnalysisService;

import com.cloudbread.domain.photo_analyses.application.event.PhotoAnalysisSseManager;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisRequest;
import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhotoAnalysisRestController {
    private final PhotoAnalysisService photoAnalysisService;
    private final PhotoAnalysisSseManager sse;

    // 1) 업로드
    @PostMapping(value = "/photo-analyses", consumes = "multipart/form-data")
    public BaseResponse<PhotoAnalysisResponse.UploadResponse> upload(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        Long userId = principal.getUserId();
        var result = photoAnalysisService.upload(userId, file);
        return BaseResponse.onSuccess(SuccessStatus.PHOTO_UPLOAD_SUCCESS, result); // 프로젝트 상수 맞춰서 사용
    }

    // 2) AI → BE 라벨 저장 (FE가 AI 대신 호출해도 됨)
    @PostMapping("/ai/photo-analyses/{photoAnalysisId}/label")
    public BaseResponse<PhotoAnalysisResponse.Ok> aiLabel(
            @PathVariable Long photoAnalysisId,
            @RequestBody @Valid PhotoAnalysisRequest.AiLabelRequest request
    ) throws Exception {
        photoAnalysisService.handleAiLabel(photoAnalysisId, request);

        return BaseResponse.onSuccess(SuccessStatus._OK, new PhotoAnalysisResponse.Ok(true));
    }

    // 3) SSE 구독 (FE는 1번 업로드 api 받고, 이걸 바로 열어둬야 함)
    @GetMapping(value = "/photo-analyses/{photoAnalysisId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long photoAnalysisId) {
        return sse.subscribe(photoAnalysisId);
    }

}
