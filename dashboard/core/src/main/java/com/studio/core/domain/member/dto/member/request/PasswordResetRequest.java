package com.studio.core.domain.member.dto.member.request;

public record PasswordResetRequest(
        String email,
        String newPassword
) {
}
