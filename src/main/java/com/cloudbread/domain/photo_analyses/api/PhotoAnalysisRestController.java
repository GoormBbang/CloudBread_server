package com.cloudbread.domain.photo_analyses.api;


import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.photo_analyses.application.PhotoAnalysisService;

import com.cloudbread.domain.photo_analyses.dto.PhotoAnalysisResponse;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhotoAnalysisRestController {
    private final PhotoAnalysisService photoAnalysisService;

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


}
