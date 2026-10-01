package com.studio.core.global.enums;

import lombok.Getter;

@Getter
public enum JoinRequestStatus {
    PENDING("대기"),
    APPROVED("승인"),
    REJECTED("거절");

    private final String description;

    JoinRequestStatus(String description) {
        this.description = description;
    }
}
