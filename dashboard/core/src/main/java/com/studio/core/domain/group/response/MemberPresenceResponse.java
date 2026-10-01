package com.studio.core.domain.group.response;

import java.time.LocalDateTime;

public record MemberPresenceResponse(
        Long memberNo,
        boolean online,
        LocalDateTime lastSeenAt
) {
    public static MemberPresenceResponse of(
            Long memberNo,
            boolean online,
            LocalDateTime lastSeenAt
    ) {
        return new MemberPresenceResponse(
                memberNo,
                online,
                online ? null : lastSeenAt
        );
    }
}
