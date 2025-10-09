package com.cloudbread.domain.user.api;

import com.cloudbread.auth.oauth2.CustomOAuth2User;
import com.cloudbread.domain.user.application.ProfileImageService;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<Map<String, String>> updateProfileImage(
            @AuthenticationPrincipal CustomOAuth2User principal,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        String url = profileImageService.updateProfileImage(principal.getUserId(), file);
        return BaseResponse.onSuccess(SuccessStatus.PROFILE_UPDATE_SUCCESS, Map.of("profileImageUrl", url));
    }
}
