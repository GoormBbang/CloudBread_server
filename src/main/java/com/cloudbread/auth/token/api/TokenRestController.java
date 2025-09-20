package com.cloudbread.auth.token.api;

import com.cloudbread.auth.token.application.TokenService;
import com.cloudbread.auth.token.dto.TokenRequestDto;
import com.cloudbread.auth.token.dto.TokenResponseDto;
import com.cloudbread.global.common.code.status.SuccessStatus;
import com.cloudbread.global.common.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/refresh-token")
public class TokenRestController {
    private final TokenService tokenService;
    @PostMapping
    public BaseResponse<TokenResponseDto.AuthenticationResponse> refreshToken(
            @RequestBody @Valid TokenRequestDto.RefreshTokenRequest request){

        TokenResponseDto.AuthenticationResponse response = tokenService.reissueToken(request.getRefreshToken());

        return BaseResponse.onSuccess(SuccessStatus.TOKEN_REISSUE_SUCCESS, response);

    }
}
