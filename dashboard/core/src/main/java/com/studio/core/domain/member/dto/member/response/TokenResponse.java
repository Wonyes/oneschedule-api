package com.studio.core.domain.member.dto.member.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
