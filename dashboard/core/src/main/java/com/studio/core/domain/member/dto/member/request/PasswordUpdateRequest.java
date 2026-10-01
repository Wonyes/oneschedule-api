package com.studio.core.domain.member.dto.member.request;

public record PasswordUpdateRequest(
        String currentPassword,
        String newPassword
) {
}
