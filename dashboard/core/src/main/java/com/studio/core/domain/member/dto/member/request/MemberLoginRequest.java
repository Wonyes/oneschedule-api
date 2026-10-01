package com.studio.core.domain.member.dto.member.request;

public record MemberLoginRequest(
        String email,
        String password
) {
}
