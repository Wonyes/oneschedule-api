package com.studio.api.domain.auth.controller;

import com.studio.api.domain.auth.service.RefreshTokenService;
import com.studio.api.global.config.CookieProperties;
import com.studio.core.domain.member.dto.member.response.TokenResponse;
import com.studio.core.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final CookieProperties cookieProperties;

    @Operation(summary = "Access-Token 재발급")
    @PostMapping("/v1/api/token-refresh")
    public ResponseEntity<SuccessResponse<Void>> refresh(
            @CookieValue(value = CookieProperties.REFRESH, required = false) String refreshToken
    ) {
        TokenResponse token = refreshTokenService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProperties.access(token.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieProperties.refresh(token.refreshToken()).toString())
                .body(SuccessResponse.ok());
    }
}
